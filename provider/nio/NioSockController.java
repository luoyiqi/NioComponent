package NioComponent.provider.nio;

import NioComponent.provider.NioTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 2014/6/30.
 */
public class NioSockController extends ANioController {

    private NioSockACRer nioSockACRer = null;
    private DataDispatcher dataDispatcher = null;
    private final Lock objLock = new ReentrantLock();


    public NioSockController() {
        super();
        mBindTcpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpServiceSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindTcpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mBindUdpConnectionSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteTcpSocks = new NioSockMap<NioSockEntity>(capacity);
        mRemoteUdpSocks = new NioSockMap<NioSockEntity>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mBindPool = new NioSockEntityPool(bindPoolCapacity, this);//handle, better way?
        mRemoteTcpReceiveQueue = new LinkedList<NioSockEntity>();
        mRemoteUdpReceiveQueue = new LinkedList<NioSockEntity>();
        mBindTcpReceiveQueue = new LinkedList<NioSockEntity>();
        mBindUdpReceiveQueue = new LinkedList<NioSockEntity>();
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
        mRemoteUdpSocks = new NioSockMap<NioSockEntity>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mBindPool = new NioSockEntityPool(bindPoolCapacity, this);//handle, better way?
        mRemoteTcpReceiveQueue = new LinkedList<NioSockEntity>();
        mRemoteUdpReceiveQueue = new LinkedList<NioSockEntity>();
        mBindTcpReceiveQueue = new LinkedList<NioSockEntity>();
        mBindUdpReceiveQueue = new LinkedList<NioSockEntity>();
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
        nioSockACRer.mPool = mPool;
        nioSockACRer.isRun = true;
        nioSockACRer.start();

        dataDispatcher = new DataDispatcher();
        dataDispatcher.mPool = mPool;
        dataDispatcher.mBindTcpReceiveQueue = mBindTcpReceiveQueue;
        dataDispatcher.mBindUdpReceiveQueue = mBindUdpReceiveQueue;
        dataDispatcher.mRemoteTcpReceiveQueue = mRemoteTcpReceiveQueue;
        dataDispatcher.mRemoteUdpReceiveQueue = mRemoteUdpReceiveQueue;
        dataDispatcher.isRun = true;
        dataDispatcher.start();
    }

    @Override
    public void notifySetEvent() {

        if (nioSockACRer != null)
        {
            nioSockACRer.exceptionMsgEvent = exceptionMsgEvent;
            nioSockACRer.operationStateEvent = operationStateEvent;
        }

        if (dataDispatcher != null)
        {
            dataDispatcher.connectionDataEvent = connectionDataEvent;
            dataDispatcher.serviceDataEvent = serviceDataEvent;
        }
    }

    @Override
    public boolean createTcpService(int bindPort) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_TCP_SERVER;// this is important, as case for thread
            nioSockEntity.bindPort = bindPort;

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

    }

    @Override
    public void removeAllTcpService() {

    }

    @Override
    public void removeRemoteTcpConnection(String ip, int port) {

    }

    @Override
    public void removeAllRemoteTcpConnection() {

    }

    @Override
    public boolean createUdpService(int bindPort) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_UDP_SERVER;// this is important, as case for thread
            nioSockEntity.bindPort = bindPort;

            DatagramChannel channel = null;

            try {
                channel = DatagramChannel.open();
                nioSockEntity.udpChannel = channel;
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(bindPort));
                mSelector.wakeup();
                channel.register(mSelector, SelectionKey.OP_ACCEPT, nioSockEntity);

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

    }

    @Override
    public void removeAllUdpService() {

    }

    @Override
    public void removeRemoteUdpConnection(String ip, int port) {

    }

    @Override
    public void removeAllRemoteUdpConnection() {

    }

    @Override
    public boolean createTcpConnection(String host, int port) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_TCP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;

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
    public void removeTcpConnection(int bindPort) {

    }

    @Override
    public void removeAllTcpConnection() {

    }

    @Override
    public boolean createUdpConnection(String host, int port) {
        boolean isSuc = false;

        NioSockEntity nioSockEntity = mBindPool.obtain();

        if (nioSockEntity != null) {
            nioSockEntity.channelType = NioTypes.TYPE_UDP_CLIENT;// this is important, as case for thread
            nioSockEntity.host = host;
            nioSockEntity.port = port;

            DatagramChannel channel = null;

            try {
                channel = DatagramChannel.open();
                nioSockEntity.udpChannel = channel;
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
    public void removeUdpConnection(int bindPort) {

    }

    @Override
    public void removeAllUdpConnection() {

    }

    @Override
    public void destroyController() {

    }


    @Override
    public void stillbirthSocket(NioSockEntity entity) {

    }

    @Override
    public void birthSocket(NioSockEntity entity) {
        switch (entity.channelType) {
            case NioTypes.TYPE_TCP_SERVER: {
                //accept client
                String key = entity.host + ":" + entity.port;
                boolean isSuc = mRemoteTcpSocks.addChannel(key, entity);

                //callback?
                break;
            }
            case NioTypes.TYPE_TCP_CLIENT: {
                // local bind
                String key = entity.bindPort + "";
                boolean isSuc = mBindTcpConnectionSocks.addChannel(key, entity);
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_SERVER: {
                //read client add or update
                String key = entity.host + ":" + entity.port;
                boolean isSuc = mRemoteUdpSocks.addChannel(key, entity);
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_CLIENT: {
                //local bind
                String key = entity.bindPort + "";
                boolean isSuc = mBindUdpConnectionSocks.addChannel(key, entity);
                //callback?
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_SERVER: {
                //read client add or update
                String key = entity.host + ":" + entity.port;
                NioSockEntity removeEntity = mRemoteUdpSocks.removeChannel(key);
                try {
                    if (removeEntity != null && removeEntity.udpChannel != null) {
                        removeEntity.udpChannel.close();
                    } else {
                        entity.udpChannel.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_CLIENT: {
                //local bind
                String key = entity.bindPort + "";
                NioSockEntity removeEntity = mBindUdpConnectionSocks.removeChannel(key);
                try {
                    if (removeEntity != null && removeEntity.udpChannel != null) {
                        removeEntity.udpChannel.close();
                    } else {
                        entity.udpChannel.close();
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
        switch (entity.channelType) {
            case NioTypes.TYPE_TCP_SERVER: {
                mRemoteTcpReceiveQueue.add(entity);
                //callback?
                break;
            }
            case NioTypes.TYPE_TCP_CLIENT: {
                // local bind
                mBindTcpReceiveQueue.add(entity);
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_SERVER: {
                //read client add or update
                mRemoteUdpReceiveQueue.add(entity);
                //callback?
                break;
            }
            case NioTypes.TYPE_UDP_CLIENT: {
                //local bind
                mBindUdpReceiveQueue.add(entity);
                //callback?
                break;
            }
        }
    }
}
