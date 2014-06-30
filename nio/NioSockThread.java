package NioComponent.nio;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by charlown on 2014/6/27.
 */
public class NioSockThread extends Thread {
    private NioSockEntityPool mPool;
    private Selector mSelector;
    private int defaultSize = 1024;
    public boolean isRun = true;

    private NioSockThread() {
        super();
    }

    public NioSockThread(NioSockEntityPool pool, Selector selector) {
        mPool = pool;
        mSelector = selector;
    }

    public NioSockThread(NioSockEntityPool pool, Selector selector, int defaultSize) {
        mPool = pool;
        mSelector = selector;
        this.defaultSize = defaultSize;
    }


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


                        NioSockEntity acceptEntity = (NioSockEntity) key.attachment();

                        if (acceptEntity != null) {

                            if (acceptEntity.channelType == NioTypes.TYPE_TCP_SERVER) {


                                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                                SocketChannel clientChannel = serverSocketChannel.accept();
                                if (clientChannel != null) {

                                    nioSockEntity = mPool.obtain();
                                    if (nioSockEntity != null) {

                                        nioSockEntity.channel = clientChannel;

                                        nioSockEntity.decodeSocketAddress(clientChannel);

                                        clientChannel.configureBlocking(false);
                                        clientChannel.register(mSelector, SelectionKey.OP_READ, acceptEntity.handle);

                                        NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;
                                        handler.birthSocket(nioSockEntity);

                                    } else {
                                        clientChannel.close();// pool empty
                                    }
                                }
                            } else if (acceptEntity.channelType == NioTypes.TYPE_UDP_SERVER) {
                                //
                            }
                        } else {
                            key.channel().close();
                        }

                    } else if (key.isConnectable()) {

                        SocketChannel channel = (SocketChannel) key.channel();
                        NioSockEntity connectNioSockEntity = (NioSockEntity) key.attachment();

                        if (channel != null) {
                            if (connectNioSockEntity != null) {

                                nioSockEntity = mPool.obtain();
                                if (nioSockEntity != null) {
                                    NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                                    try {
                                        if (channel.finishConnect()) {


                                            if (channel.isConnected()) {

                                                channel.register(mSelector, SelectionKey.OP_READ, connectNioSockEntity.handle);
                                                handler.birthSocket(nioSockEntity);

                                            }
                                            else
                                            {
                                                //send msg notify?
                                                mPool.recovery(nioSockEntity);
                                            }
                                        }
                                        else {
                                            //send mag notify?
                                            mPool.recovery(nioSockEntity);
                                        }

                                    } catch (ConnectException ce) {

                                        //need callback
                                        handler.stillbirthSocket(connectNioSockEntity);
                                        mPool.recovery(connectNioSockEntity);


                                    }
                                } else {
                                    //need to send msg to know pool empty
                                }
                            } else {
                                channel.close();
                            }
                        }


                    } else if (key.isReadable()) {


                        SocketChannel channel = (SocketChannel) key.channel();

                        mBuffer.clear();

                        int rs = channel.read(mBuffer);

                        nioSockEntity = mPool.obtain();

                        if (nioSockEntity != null) {

                            NioSockEntity.INioSockEventHandler handler = (NioSockEntity.INioSockEventHandler) nioSockEntity.handle;

                            nioSockEntity.decodeSocketAddress(channel);

                            if (rs > 0) {
                                mBuffer.flip();

                                nioSockEntity.dataBuffer.put(mBuffer);
                                nioSockEntity.channel = channel;


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
                            //mPool empty
                            channel.close();
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
