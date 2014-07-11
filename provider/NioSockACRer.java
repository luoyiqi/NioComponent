package NioComponent.provider;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by charlown on 2014/6/27.
 */
public class NioSockACRer extends Thread {
    public NioSockEntityPool mPool;
    public Selector mSelector;
    public int defaultSize = 1024;
    public boolean isRun = true;
    public INotifyExceptionMsgHandler exceptionMsgEvent;


    @Override
    public void run() {


        ByteBuffer mBuffer = ByteBuffer.allocate(defaultSize);
        int numKeys = 0;
        NioSockEntity nioSockEntity = null;

        try {

            while (isRun) {


                numKeys = mSelector.select();

                if (numKeys <= 0) {
                    /**
                     * if enter this,  selector wake up must be used somewhere.
                     */

/*
                    while (true) {

                        if (mSelector != null) {
                            if (mSelector.isOpen()) {
                                if (!mSelector.keys().isEmpty()) {
                                    break;
                                }
                            }
                        }

                        if (!isRun) {
                            break;
                        }
                    }
                    */
                    //above way test fail: when server create, follow client create, add client key may be empty, and then running until server has key to wake up this thread.
                    sleep(100); //has a good way to instead of ?
                }

                if (!mSelector.isOpen())
                    continue;

                Set<SelectionKey> selectionKeySet = mSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {


                        NioSockEntity seed = (NioSockEntity) key.attachment();

                        if (seed != null) {

                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel clientChannel = serverSocketChannel.accept();
                            if (clientChannel != null) {

                                nioSockEntity = mPool.obtain();
                                if (nioSockEntity != null) {

                                    nioSockEntity.channelType = seed.channelType;
                                    nioSockEntity.tcpChannel = clientChannel;

                                    nioSockEntity.decodeSocketAddress(clientChannel);

                                    clientChannel.configureBlocking(false);
                                    clientChannel.register(mSelector, SelectionKey.OP_READ, seed);

                                    NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;
                                    nioSockEntity.handle = seed.handle;
                                    handler.birthSocket(nioSockEntity);

                                }
                            }

                        } else {
                            key.channel().close();
                        }

                    } else if (key.isConnectable()) {

                        SocketChannel channel = (SocketChannel) key.channel();
                        NioSockEntity seed = (NioSockEntity) key.attachment();
                        nioSockEntity = mPool.obtain();//only use nioSockEntity handler

                        if (nioSockEntity != null) {


                            NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                            try {
                                if (channel.finishConnect()) {


                                    if (channel.isConnected()) {

                                        seed.decodeSocketAddress(channel);

                                        channel.register(mSelector, SelectionKey.OP_READ, seed);//instead of connect key
                                        handler.birthSocket(seed);

                                    } else {
                                        //send msg notify?
                                        mPool.recovery(nioSockEntity);
                                    }
                                } else {
                                    //send mag notify?
                                    mPool.recovery(nioSockEntity);
                                }

                            } catch (ConnectException ce) {

                                //need callback
                                handler.stillbirthSocket(seed);
                                mPool.recovery(seed);


                            }


                        } else {
                            key.channel().close();
                        }

                        mPool.recovery(nioSockEntity);//only use nioSockEntity handler


                    } else if (key.isReadable()) {


                        NioSockEntity seed = (NioSockEntity) key.attachment();

                        if (seed != null) {
                            mBuffer.clear();


                            switch (seed.channelType) {
                                case NioTypes.TYPE_TCP_SERVER:
                                case NioTypes.TYPE_TCP_CLIENT: {
                                    SocketChannel channel = (SocketChannel) key.channel();
                                    int rs = channel.read(mBuffer);

                                    nioSockEntity = mPool.obtain();

                                    if (nioSockEntity != null) {
                                        NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                        nioSockEntity.handle = seed.handle;//notify:change handler!!!

                                        nioSockEntity.channelType = seed.channelType;
                                        nioSockEntity.tcpChannel = channel;
                                        nioSockEntity.decodeSocketAddress(channel);

                                        if (rs > 0) {
                                            mBuffer.flip();

                                            nioSockEntity.setBuffer(mBuffer);


                                            if (handler != null) {
                                                handler.birthBuffer(nioSockEntity);
                                            } else {
                                                mPool.recovery(nioSockEntity);
                                            }

                                        } else if (rs == 0) {
                                            //?
                                            mPool.recovery(nioSockEntity);
                                        } else {
                                            //remote socket close.
                                            if (handler != null) {
                                                handler.deadSocket(nioSockEntity);
                                            }
                                            mPool.recovery(nioSockEntity);
                                        }

                                    } else {
                                        //pool empty
                                    }


                                    break;
                                }
                                case NioTypes.TYPE_UDP_SERVER:
                                case NioTypes.TYPE_UDP_CLIENT: {
                                    DatagramChannel channel = (DatagramChannel) key.channel();

                                    SocketAddress address = channel.receive(mBuffer);
                                    if (mBuffer.hasRemaining()) {


                                        nioSockEntity = mPool.obtain();

                                        if (nioSockEntity != null) {
                                            NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                            nioSockEntity.handle = seed.handle;//notify:change handler!!!

                                            nioSockEntity.channelType = seed.channelType;
                                            nioSockEntity.udpChannel = channel;
                                            nioSockEntity.bindPort = seed.bindPort;
                                            nioSockEntity.host = ((InetSocketAddress) address).getAddress().getHostAddress();
                                            nioSockEntity.port = ((InetSocketAddress) address).getPort();


                                            mBuffer.flip();


                                            nioSockEntity.setBuffer(mBuffer);


                                            if (handler != null) {
                                                handler.birthBuffer(nioSockEntity);
                                            } else {
                                                mPool.recovery(nioSockEntity);
                                            }

                                        } else {
                                            //pool empty
                                        }
                                        break;
                                    }
                                }
                            }

                        } else {
                            key.channel().close();
                        }

                    }

                    iterator.remove();

                }
            }


        } catch (IOException ioe) {
            if (nioSockEntity != null)
                mPool.recovery(nioSockEntity);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
