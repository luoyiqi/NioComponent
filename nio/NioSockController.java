package NioComponent.nio;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 2014/6/30.
 */
public class NioSockController extends ANioController {

    private NioSockThread nioSockThread = null;
    private final Lock objLock = new ReentrantLock();

    public NioSockController() {
        super();
        mBindSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteSocks = new NioSockMap<NioSockEntity>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }

    public NioSockController(int capacity, int poolCapacity) {
        super();
        this.capacity = capacity;
        this.poolCapacity = poolCapacity;
        mBindSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteSocks = new NioSockMap<NioSockEntity>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }

    @Override
    public void init() {
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nioSockThread = new NioSockThread(mPool, mSelector);
        nioSockThread.isRun = true;
        nioSockThread.start();
    }

    @Override
    public boolean createTcpService(int bindPort) {
        boolean isSuc = false;


        NioSockEntity nioSockEntity = mPool.obtain();

        if (nioSockEntity != null) {

            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


                serverSocketChannel.socket().bind(new InetSocketAddress(bindPort));
                serverSocketChannel.configureBlocking(false);

                nioSockEntity.channelType = NioTypes.TYPE_TCP_SERVER;
                nioSockEntity.channelTcpServer = serverSocketChannel;
                nioSockEntity.bindPort = bindPort;
                nioSockEntity.handle = notifyEvent;

                mSelector.wakeup();
                serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT, nioSockEntity);


                String key = serverSocketChannel.socket().getLocalPort() + "";

                isSuc = mBindSocks.addChannel(key, nioSockEntity);


            } catch (BindException be) {
                mPool.recovery(nioSockEntity);
                //callback msg
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isSuc;
    }

    @Override
    public void removeTcpService(int bindPort) {
        String key = bindPort + "";

        try {
            objLock.tryLock(1, TimeUnit.SECONDS);

            NioSockEntity nioSockEntity = mBindSocks.removeChannel(key);
            if (nioSockEntity != null) {
                if (nioSockEntity.channelTcpServer != null) {
                    try {
                        ServerSocketChannel channel = nioSockEntity.channelTcpServer;
                        channel.close();
                    } catch (IOException e) {
                        //callback msg
                        e.printStackTrace();
                    }
                }

                mPool.recovery(nioSockEntity);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllTcpService() {

        try {
            objLock.tryLock(1, TimeUnit.SECONDS);
            Collection<NioSockEntity> collection = mBindSocks.getChannels();
            ArrayList<String> keys = new ArrayList<String>();

            for (NioSockEntity entity : collection) {
                if (entity.channelType == NioTypes.TYPE_TCP_SERVER) {
                    keys.add(entity.bindPort + "");
                }
            }

            for(String key: keys)
            {
                NioSockEntity nioSockEntity = mBindSocks.removeChannel(key);
                if (nioSockEntity != null && nioSockEntity.channelTcpServer != null)
                {
                    nioSockEntity.channelTcpServer.close();
                    mPool.recovery(nioSockEntity);
                }

            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }

    }

    @Override
    public boolean createUdpService(int bindPort) {
        return false;
    }

    @Override
    public void removeUdpService(int bindPort) {

    }

    @Override
    public void removeAllUdpService() {

    }

    @Override
    public void removeRemoteConnection(String ip, int port) {
        try {
            String key = ip + ":" + port;

            objLock.tryLock(1, TimeUnit.SECONDS);

            NioSockEntity nioSockEntity = mRemoteSocks.removeChannel(key);

            if (nioSockEntity != null && nioSockEntity.channel != null)
            {
                nioSockEntity.channel.close();
                mPool.recovery(nioSockEntity);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllRemoteConnection() {
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);

            Collection<NioSockEntity> collection = mRemoteSocks.getChannels();

            for(NioSockEntity entity:collection)
            {
                if (entity != null && entity.channel != null)
                {
                    entity.channel.close();
                }
                mPool.recovery(entity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public boolean createConnection(String host, int port) {
        boolean isSuc = false;

        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);


            NioSockEntity nioSockEntity = mPool.obtain();

            if (nioSockEntity != null) {

                nioSockEntity.handle = notifyEvent;
                nioSockEntity.port = port;
                nioSockEntity.host = host;
                nioSockEntity.channel = channel;

                channel.connect(new InetSocketAddress(nioSockEntity.host, nioSockEntity.port));

                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_CONNECT, nioSockEntity);

                isSuc = true;
            }


        } catch (IOException ie) {
           ie.printStackTrace();
        }

        return isSuc;
    }

    @Override
    public void removeLocalConnection(int bindPort) {

        try
        {
            String key = bindPort +"";

            objLock.tryLock(1, TimeUnit.SECONDS);

            NioSockEntity nioSockEntity =  mBindSocks.removeChannel(key);
            if (nioSockEntity != null && nioSockEntity.channel != null)
            {
                nioSockEntity.channel.close();

                mPool.recovery(nioSockEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllConnection() {
        try
        {
            objLock.tryLock(1, TimeUnit.SECONDS);

           Collection<NioSockEntity> collection = mRemoteSocks.getChannels();

            for(NioSockEntity entity:collection)
            {
                if (entity != null && entity.channel != null)
                {
                    entity.channel.close();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void destroyController() {


        removeAllConnection();
        removeAllRemoteConnection();
        removeAllTcpService();
        removeAllUdpService();

        mRemoteSocks.clear();
        mBindSocks.clear();
        mReceiveQueue.clear();
        mPool.onDestroy();

        if (nioSockThread != null)
        {
            nioSockThread.isRun = false;
        }
        nioSockThread = null;

        if (mSelector.isOpen())
        {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void stillbirthSocket(NioSockEntity entity) {

    }

    @Override
    public void birthSocket(NioSockEntity entity) {

    }

    @Override
    public void deadSocket(NioSockEntity entity) {

    }

    @Override
    public void birthBuffer(NioSockEntity entity) {

    }
}
