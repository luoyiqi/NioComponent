package NioComponent.nio;

import java.io.IOException;
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

                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverSocketChannel.accept();

                        if (clientChannel != null) {

                            nioSockEntity = mPool.obtain();
                            if (nioSockEntity != null) {


                                nioSockEntity.channel = clientChannel;

                                nioSockEntity.decodeSocketAddress(clientChannel);

                                clientChannel.configureBlocking(false);
                                clientChannel.register(mSelector, SelectionKey.OP_READ);
                                INioSockEventHandler handler = (INioSockEventHandler)key.attachment();
                                if (handler != null)
                                {
                                    handler.addClientMap(nioSockEntity);
                                }
                                else
                                {
                                    mPool.recovery(nioSockEntity);
                                }

                            } else {
                                clientChannel.close();// pool empty
                            }
                        }


                    }else if (key.isConnectable())
                    {
                        SocketChannel channel = (SocketChannel)key.channel();

                        if (channel != null)
                        {
                            nioSockEntity = mPool.obtain();
                            if (nioSockEntity != null)
                            {
                                nioSockEntity.channel = channel;
                                nioSockEntity.decodeSocketAddress(channel);

                                channel.register(mSelector, SelectionKey.OP_READ);
                                INioSockEventHandler handler = (INioSockEventHandler)key.attachment();
                                if (handler != null)
                                {
                                    handler.addClientMap(nioSockEntity);
                                }
                                else
                                {
                                    mPool.recovery(nioSockEntity);
                                }
                            }
                            else
                            {
                                channel.close();
                            }
                        }
                    }
                    else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        mBuffer.clear();
                        int rs = channel.read(mBuffer);
                        nioSockEntity = mPool.obtain();

                        if (nioSockEntity != null) {

                            nioSockEntity.decodeSocketAddress(channel);

                            if (rs > 0) {
                                mBuffer.flip();

                                nioSockEntity.dataBuffer.put(mBuffer);
                                nioSockEntity.channel = channel;

                                INioSockEventHandler handler = (INioSockEventHandler)key.attachment();
                                if (handler != null)
                                {
                                    handler.addReceiveBufferQueue(nioSockEntity);
                                }
                                else
                                {
                                    mPool.recovery(nioSockEntity);
                                }

                            } else if (rs == 0) {
                                //?
                                mPool.recovery(nioSockEntity);
                            } else {
                                //remote socket close.
                                INioSockEventHandler handler = (INioSockEventHandler)key.attachment();
                                if (handler != null)
                                {
                                    handler.removeClient(nioSockEntity.bindPort, nioSockEntity.host, nioSockEntity.port);
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
