package NioComponent.provider;

import java.io.IOException;
import java.net.ConnectException;
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
    public INotifyOperationStateHandler operationStateEvent;



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
                }

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
                                    handler.birthSocket(nioSockEntity);

                                }
                            }

                        } else {
                            key.channel().close();
                        }

                    } else if (key.isConnectable()) {

                        SocketChannel channel = (SocketChannel) key.channel();
                        NioSockEntity seed = (NioSockEntity) key.attachment();


                        if (seed != null) {

                            nioSockEntity = mPool.obtain();
                            if (nioSockEntity != null) {
                                NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                try {
                                    if (channel.finishConnect()) {


                                        if (channel.isConnected()) {

                                            channel.register(mSelector, SelectionKey.OP_READ, seed);//instead of connect key
                                            handler.birthSocket(nioSockEntity);

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
                                //need to send msg to know pool empty
                            }

                        } else {
                            key.channel().close();
                        }


                    } else if (key.isReadable()) {


                        // SocketChannel channel = (SocketChannel) key.channel();
                        NioSockEntity seed = (NioSockEntity) key.attachment();

                        if (seed != null) {
                            mBuffer.clear();
                            int rs;

                            switch (seed.channelType) {
                                case NioTypes.TYPE_TCP_SERVER:
                                case NioTypes.TYPE_TCP_CLIENT: {
                                    SocketChannel channel = (SocketChannel) key.channel();
                                    rs = channel.read(mBuffer);

                                    nioSockEntity = mPool.obtain();

                                    if (nioSockEntity != null) {
                                        NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                        nioSockEntity.channelType = seed.channelType;
                                        nioSockEntity.tcpChannel = channel;
                                        nioSockEntity.decodeSocketAddress(channel);

                                        if (rs > 0) {
                                            mBuffer.flip();

                                            nioSockEntity.dataBuffer.put(mBuffer);
                                            nioSockEntity.tcpChannel = channel;


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
                                    rs = channel.read(mBuffer);

                                    nioSockEntity = mPool.obtain();

                                    if (nioSockEntity != null) {
                                        NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                        nioSockEntity.channelType = seed.channelType;
                                        nioSockEntity.udpChannel = channel;
                                        nioSockEntity.decodeSocketAddress(channel);

                                        if (rs > 0) {
                                            mBuffer.flip();

                                            nioSockEntity.dataBuffer.put(mBuffer);
                                            nioSockEntity.udpChannel = channel;


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


        }

    }


}
