package NioComponent.provider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 2014/6/30.
 */
public class NioSockController extends ANioController {

    private NioSockACRer nioSockACRer = null;
    private DataDispatcher dataDispatcher = null;
    private NioSockSender nioSockSender = null;
    private final Lock objLock = new ReentrantLock();


    public NioSockController() {
        super();
        mBindTcpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindTcpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteTcpSocks = new NioSockMap<NioSockEntity>(capacity);
        mReadPool = new NioSockEntityPool(poolCapacity, this);
        mWritePool = new NioSockEntityPool(poolCapacity, this);
        mBindPool = new NioSockEntityPool(bindPoolCapacity, this);//handle, better way?
        mReceiveQueue = new ArrayBlockingQueue<NioSockEntity>(4 * capacity); //all  receive

        mSendQueue = new ArrayBlockingQueue<NioSockEntity>(10 * capacity);
    }

    public NioSockController(int capacity, int poolCapacity, int bindPoolCapacity) {
        super();
        this.capacity = capacity;
        this.poolCapacity = poolCapacity;
        this.bindPoolCapacity = bindPoolCapacity;
        mBindTcpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindTcpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteTcpSocks = new NioSockMap<NioSockEntity>(capacity);
        mReadPool = new NioSockEntityPool(poolCapacity, this);
        mWritePool = new NioSockEntityPool(poolCapacity, this);
        mBindPool = new NioSockEntityPool(bindPoolCapacity, this);//handle, better way?
        mReceiveQueue = new ArrayBlockingQueue<NioSockEntity>(4 * capacity); //all  receive

        mSendQueue = new ArrayBlockingQueue<NioSockEntity>(10 * capacity);
    }

    public NioSockController(int capacity, int poolCapacity, int bindPoolCapacity, int bufferSize) {
        super();
        this.capacity = capacity;
        this.poolCapacity = poolCapacity;
        this.bindPoolCapacity = bindPoolCapacity;
        mBindTcpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindTcpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteTcpSocks = new NioSockMap<NioSockEntity>(capacity);
        mReadPool = new NioSockEntityPool(poolCapacity, bufferSize, this);
        mWritePool = new NioSockEntityPool(poolCapacity, bufferSize, this);
        mBindPool = new NioSockEntityPool(bindPoolCapacity, bufferSize, this);//handle, better way?
        mReceiveQueue = new ArrayBlockingQueue<NioSockEntity>(4 * capacity); //all  receive

        mSendQueue = new ArrayBlockingQueue<NioSockEntity>(10 * capacity);
    }


    @Override
    public void init() {
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nioSockACRer = new NioSockACRer();
        nioSockACRer.mSelector = mSelector;
        nioSockACRer.mPool = mReadPool;
        nioSockACRer.isRun = true;
        nioSockACRer.exceptionMsgEvent = exceptionMsgEvent;
        nioSockACRer.start();

        dataDispatcher = new DataDispatcher();
        dataDispatcher.mPool = mReadPool;

        dataDispatcher.mReceiveQueue = mReceiveQueue;
        dataDispatcher.isRun = true;
        dataDispatcher.start();

        nioSockSender = new NioSockSender();
        nioSockSender.isRun = true;
        nioSockSender.mPool = mWritePool;
        nioSockSender.sendCache = mSendQueue;
        nioSockSender.start();


    }

    @Override
    public void init(int capacity) {
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nioSockACRer = new NioSockACRer(capacity);
        nioSockACRer.mSelector = mSelector;
        nioSockACRer.mPool = mReadPool;
        nioSockACRer.isRun = true;
        nioSockACRer.exceptionMsgEvent = exceptionMsgEvent;
        nioSockACRer.start();

        dataDispatcher = new DataDispatcher();
        dataDispatcher.mPool = mReadPool;

        dataDispatcher.mReceiveQueue = mReceiveQueue;
        dataDispatcher.isRun = true;
        dataDispatcher.start();

        nioSockSender = new NioSockSender();
        nioSockSender.isRun = true;
        nioSockSender.mPool = mWritePool;
        nioSockSender.sendCache = mSendQueue;
        nioSockSender.start();

    }


    @Override
    public boolean createTcpService(int bindPort, INotifyServiceEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_TCP_SERVER;// this is important, as case for thread
            nioSockEntity.bindPort = bindPort;
            nioSockEntity.handle = handler;

            ServerSocketChannel channel = null;

            try {
                channel = ServerSocketChannel.open();
                nioSockEntity.tcpChannelServer = channel;
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(bindPort));
                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_ACCEPT, nioSockEntity);

                isSuc = mBindTcpServiceSocks.addChannel(nioSockEntity.bindPort + "", nioSockEntity);

            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }

    @Override
    public void removeTcpService(int bindPort) {

        // this interface give out to control, must be locked.

        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity removeEntity = mBindTcpServiceSocks.removeChannel(bindPort + "");

            if (removeEntity != null && removeEntity.tcpChannelServer != null) {
                removeEntity.tcpChannelServer.close();

                mBindPool.recovery(removeEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

    }

    @Override
    public void removeAllTcpService() {
        // this interface give out to control, must be locked.

        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            Collection<NioSockEntity> collection = mBindTcpServiceSocks.getChannels();

            for (NioSockEntity removeEntity : collection) {
                if (removeEntity != null && removeEntity.tcpChannelServer != null) {
                    removeEntity.tcpChannelServer.close();

                    mBindPool.recovery(removeEntity);
                }
            }
            mBindTcpServiceSocks.clear();


        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

    }

    @Override
    public void removeRemoteTcpConnection(String ip, int port) {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity removeEntity = mRemoteTcpSocks.removeChannel(ip + ":" + port);

            if (removeEntity != null && removeEntity.tcpChannel != null) {
                removeEntity.tcpChannel.close();

                mBindPool.recovery(removeEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllRemoteTcpConnection() {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            Collection<NioSockEntity> collection = mRemoteTcpSocks.getChannels();

            for (NioSockEntity removeEntity : collection) {
                if (removeEntity != null && removeEntity.tcpChannel != null) {
                    removeEntity.tcpChannel.close();

                    mBindPool.recovery(removeEntity);
                }
            }
            mRemoteTcpSocks.clear();

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public boolean createUdpService(int bindPort, INotifyServiceEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_UDP_SERVER;// this is important, as case for thread
            nioSockEntity.bindPort = bindPort;
            nioSockEntity.handle = handler;

            DatagramChannel channel = null;

            try {
                channel = DatagramChannel.open();
                nioSockEntity.udpChannel = channel;
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(bindPort));
                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_READ, nioSockEntity);

                isSuc = mBindUdpServiceSocks.addChannel(nioSockEntity.bindPort + "", nioSockEntity);


            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }

    @Override
    public void removeUdpService(int bindPort) {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity removeEntity = mBindUdpServiceSocks.removeChannel(bindPort + "");

            if (removeEntity != null && removeEntity.udpChannel != null) {
                removeEntity.udpChannel.close();

                mBindPool.recovery(removeEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

    }

    @Override
    public void removeAllUdpService() {
        // this interface give out to control, must be locked.

        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            Collection<NioSockEntity> collection = mBindUdpServiceSocks.getChannels();

            for (NioSockEntity removeEntity : collection) {
                if (removeEntity != null && removeEntity.udpChannel != null) {
                    removeEntity.udpChannel.close();

                    mBindPool.recovery(removeEntity);
                }
            }
            mBindUdpServiceSocks.clear();

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }


    @Override
    public boolean createTcpConnection(String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_TCP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;
            nioSockEntity.handle = handler;

            SocketChannel channel = null;

            try {
                channel = SocketChannel.open();
                nioSockEntity.tcpChannel = channel;
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(host, port));

                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_CONNECT, nioSockEntity);

                isSuc = true;

            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }

    @Override
    public boolean createTcpConnection(int bindPort, String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_TCP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;
            nioSockEntity.handle = handler;

            SocketChannel channel = null;

            try {
                channel = SocketChannel.open();
                nioSockEntity.tcpChannel = channel;
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(bindPort));
                channel.connect(new InetSocketAddress(host, port));


                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_CONNECT, nioSockEntity);

                isSuc = true;

            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }


    @Override
    public void removeTcpConnection(int bindPort) {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity removeEntity = mBindTcpConnectionSocks.removeChannel(bindPort + "");

            if (removeEntity != null && removeEntity.tcpChannel != null) {
                removeEntity.tcpChannel.close();

                mBindPool.recovery(removeEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllTcpConnection() {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            Collection<NioSockEntity> collection = mBindTcpConnectionSocks.getChannels();

            for (NioSockEntity removeEntity : collection) {
                if (removeEntity != null && removeEntity.tcpChannel != null) {
                    removeEntity.tcpChannel.close();

                    mBindPool.recovery(removeEntity);
                }
            }
            mBindTcpConnectionSocks.clear();

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public boolean createUdpConnection(String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_UDP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;
            nioSockEntity.handle = handler;

            DatagramChannel channel = null;

            try {
                channel = DatagramChannel.open();
                nioSockEntity.udpChannel = channel;
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(host, port));

                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_READ, nioSockEntity);


                nioSockEntity.bindPort = channel.socket().getLocalPort();


                isSuc = mBindUdpConnectionSocks.addChannel(nioSockEntity.bindPort + "", nioSockEntity);

                if (handler != null) {
                    if (isSuc)
                        handler.notifyCreateConnection(NioTypes.TYPE_UDP_CLIENT, nioSockEntity.bindPort, host, port);
                    else
                        handler.notifyDisconnect(NioTypes.TYPE_UDP_CLIENT, nioSockEntity.bindPort, host, port);
                }

            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }

    @Override
    public boolean createUdpConnection(int bindPort, String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_UDP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;
            nioSockEntity.bindPort = bindPort;
            nioSockEntity.handle = handler;

            DatagramChannel channel = null;

            try {
                channel = DatagramChannel.open();
                nioSockEntity.udpChannel = channel;
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(bindPort));
                channel.connect(new InetSocketAddress(host, port));

                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_READ, nioSockEntity);


                isSuc = mBindUdpConnectionSocks.addChannel(nioSockEntity.bindPort + "", nioSockEntity);
                if (handler != null) {
                    if (isSuc)
                        handler.notifyCreateConnection(NioTypes.TYPE_UDP_CLIENT, nioSockEntity.bindPort, host, port);
                    else
                        handler.notifyDisconnect(NioTypes.TYPE_UDP_CLIENT, nioSockEntity.bindPort, host, port);
                }

            } catch (IOException e) {
                e.printStackTrace();
                //callback?
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                mBindPool.recovery(nioSockEntity);
            }
        }

        return isSuc;
    }


    @Override
    public void removeUdpConnection(int bindPort) {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity removeEntity = mBindUdpConnectionSocks.removeChannel(bindPort + "");

            if (removeEntity != null && removeEntity.udpChannel != null) {
                removeEntity.udpChannel.close();

                mBindPool.recovery(removeEntity);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public void removeAllUdpConnection() {
        // this interface give out to control, must be locked.
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            Collection<NioSockEntity> collection = mBindUdpConnectionSocks.getChannels();

            for (NioSockEntity removeEntity : collection) {
                if (removeEntity != null && removeEntity.udpChannel != null) {
                    removeEntity.udpChannel.close();

                    mBindPool.recovery(removeEntity);
                }
            }
            mBindUdpConnectionSocks.clear();

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } catch (IOException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }
    }

    @Override
    public SocketChannel getConnectionSocketChannel(int bindPort) {
        // this interface give out to control, must be locked.
        SocketChannel channel = null;
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity nioSockEntity = mBindTcpConnectionSocks.getChannel(bindPort + "");
            if (nioSockEntity != null) {
                channel = nioSockEntity.tcpChannel;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

        return channel;
    }

    @Override
    public SocketChannel getRemoteConnectionSocketChannel(String host, int port) {
        // this interface give out to control, must be locked.
        SocketChannel channel = null;
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity nioSockEntity = mRemoteTcpSocks.getChannel(host + ":" + port);
            if (nioSockEntity != null) {
                channel = nioSockEntity.tcpChannel;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

        return channel;
    }

    @Override
    public DatagramChannel getServiceDatagramChannel(int bindPort) {
        // this interface give out to control, must be locked.
        DatagramChannel channel = null;
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity nioSockEntity = mBindUdpServiceSocks.getChannel(bindPort + "");
            if (nioSockEntity != null) {
                channel = nioSockEntity.udpChannel;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

        return channel;
    }

    @Override
    public DatagramChannel getConnectionDatagramChannel(int bindPort) {
        // this interface give out to control, must be locked.
        DatagramChannel channel = null;
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);


            NioSockEntity nioSockEntity = mBindUdpConnectionSocks.getChannel(bindPort + "");
            if (nioSockEntity != null) {
                channel = nioSockEntity.udpChannel;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //callback?
        } finally {
            objLock.unlock();
        }

        return channel;
    }

    @Override
    public void addBufferToSend(int type, SocketChannel channel, byte[] data, int dataSize) {

        NioSockEntity nioSockEntity = mWritePool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = type;
            nioSockEntity.tcpChannel = channel;
            nioSockEntity.setBuffer(data, dataSize);

            mSendQueue.add(nioSockEntity);
        }

    }

    @Override
    public void addBufferToSend(int type, DatagramChannel channel, byte[] data, int dataSize, String host, int port) {
        NioSockEntity nioSockEntity = mWritePool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = type;
            nioSockEntity.udpChannel = channel;
            nioSockEntity.host = host;
            nioSockEntity.port = port;
            nioSockEntity.setBuffer(data, dataSize);

            mSendQueue.add(nioSockEntity);
        }
    }

    @Override
    public void destroyController() {

        if (nioSockSender != null) {
            nioSockSender.isRun = false;
        }


        if (nioSockACRer != null) {
            nioSockACRer.isRun = false;
            mSelector.wakeup();
        }


        if (dataDispatcher != null) {
            dataDispatcher.isRun = false;
        }

        removeAllTcpService();
        removeAllUdpService();
        removeAllRemoteTcpConnection();
        removeAllTcpConnection();
        removeAllUdpConnection();
        mReceiveQueue.clear();

        mSendQueue.clear();
        mBindPool.onDestroy();
        mReadPool.onDestroy();
        mWritePool.onDestroy();
        try {
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void stillbirthSocket(NioSockEntity entity) {

        switch (entity.channelType) {
            case NioTypes.TYPE_TCP_CLIENT: {
                try {
                    entity.tcpChannel.close();
                    //callback?
                    INotifyConnectionEventHandler handler = (INotifyConnectionEventHandler) entity.handle;
                    if (handler != null) {
                        handler.notifyDisconnect(entity.channelType, entity.bindPort, entity.host, entity.port);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBindPool.recovery(entity);
                break;
            }
        }
    }

    @Override
    public void birthSocket(NioSockEntity entity) {
        switch (entity.channelType) {
            case NioTypes.TYPE_TCP_SERVER: {
                //accept client
                String key = entity.host + ":" + entity.port;
                boolean isSuc = mRemoteTcpSocks.addChannel(key, entity);
                //notify: this handler had instead before accept
                INotifyServiceEventHandler handler = (INotifyServiceEventHandler) entity.handle;
                if (handler != null)
                    handler.notifyCreateRemoteConnection(entity.channelType, entity.bindPort, entity.host, entity.port);

                break;
            }
            case NioTypes.TYPE_TCP_CLIENT: {
                // local bind
                String key = entity.bindPort + "";
                boolean isSuc = mBindTcpConnectionSocks.addChannel(key, entity);
                INotifyConnectionEventHandler handler = (INotifyConnectionEventHandler) entity.handle;
                if (handler != null) {
                    if (isSuc)
                        handler.notifyCreateConnection(entity.channelType, entity.bindPort, entity.host, entity.port);
                    else
                        handler.notifyDisconnect(entity.channelType, entity.bindPort, entity.host, entity.port);
                }

                break;
            }
        }
    }

    @Override
    public void deadSocket(NioSockEntity entity) {
        //tcp | udp server bind channel is not include here
        switch (entity.channelType) {
            case NioTypes.TYPE_TCP_SERVER: {
                //accept client
                String key = entity.host + ":" + entity.port;
                NioSockEntity removeEntity = mRemoteTcpSocks.removeChannel(key);
                try {
                    if (removeEntity != null && removeEntity.tcpChannel != null) {
                        removeEntity.tcpChannel.close();
                    } else {
                        entity.tcpChannel.close();
                    }

                    if (entity.handle instanceof INotifyServiceEventHandler) {
                        INotifyServiceEventHandler handler = (INotifyServiceEventHandler) entity.handle;

                        handler.notifyRemoteDisconnect(entity.channelType, entity.bindPort, entity.host, entity.port);

                    }

                    mReadPool.recovery(entity);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //callback?
                break;
            }
            case NioTypes.TYPE_TCP_CLIENT: {
                // local bind
                String key = entity.bindPort + "";
                NioSockEntity removeEntity = mBindTcpConnectionSocks.removeChannel(key);
                try {
                    if (removeEntity != null && removeEntity.tcpChannel != null) {
                        removeEntity.tcpChannel.close();
                    } else {
                        entity.tcpChannel.close();
                    }

                    INotifyConnectionEventHandler handler = (INotifyConnectionEventHandler) entity.handle;
                    if (handler != null) {
                        handler.notifyDisconnect(entity.channelType, entity.bindPort, entity.host, entity.port);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //callback?
                break;
            }

        }
    }

    @Override
    public void birthBuffer(NioSockEntity entity) {

        mReceiveQueue.add(entity);
    }
}
