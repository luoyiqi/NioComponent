package NioComponent.provider;

import java.nio.channels.Selector;
import java.util.Queue;

/**
 * Created by charlown on 2014/6/30.
 */
public abstract class ANioController implements NioSockEntity.INioSockEventHandler {
    protected int capacity = 1024;
    protected int poolCapacity = 4 * 1024;//buffer:4096
    protected int bindPoolCapacity = 1024;// sever && client: 1024

    protected NioSockMap<NioSockEntity> mBindTcpServiceSocks;
    protected NioSockMap<NioSockEntity> mBindTcpConnectionSocks;
    protected NioSockMap<NioSockEntity> mBindUdpServiceSocks;
    protected NioSockMap<NioSockEntity> mBindUdpConnectionSocks;
    protected NioSockMap<NioSockEntity> mRemoteTcpSocks;
    protected NioSockMap<NioSockEntity> mRemoteUdpSocks;
    protected NioSockEntityPool mPool, mBindPool;
    protected Queue<NioSockEntity> mBindTcpReceiveQueue;
    protected Queue<NioSockEntity> mBindUdpReceiveQueue;
    protected Queue<NioSockEntity> mRemoteTcpReceiveQueue;
    protected Queue<NioSockEntity> mRemoteUdpReceiveQueue;

    protected Selector mSelector;

    protected INotifyServiceDataHandler serviceDataEvent;
    protected INotifyConnectionDataHandler connectionDataEvent;
    protected INotifyExceptionMsgHandler exceptionMsgEvent;
    protected INotifyOperationStateHandler operationStateEvent;

    public void addNotifyHandler(INotifyServiceDataHandler eventHandler) {
        serviceDataEvent = eventHandler;
    }
    public void addNotifyHandler(INotifyConnectionDataHandler eventHandler) {
        connectionDataEvent = eventHandler;
    }
    public void addNotifyHandler(INotifyExceptionMsgHandler eventHandler) {
        exceptionMsgEvent = eventHandler;
    }
    public void addNotifyHandler(INotifyOperationStateHandler eventHandler) {
        operationStateEvent = eventHandler;
    }



    public abstract void init();
    public abstract void notifySetEvent();


    public abstract boolean createTcpService(int bindPort);
    public abstract void removeTcpService(int bindPort);
    public abstract void removeAllTcpService();
    public abstract void removeRemoteTcpConnection(String ip, int port);
    public abstract void removeAllRemoteTcpConnection();


    public abstract boolean createUdpService(int bindPort);
    public abstract void removeUdpService(int bindPort);
    public abstract void removeAllUdpService();
    public abstract void removeRemoteUdpConnection(String ip, int port);
    public abstract void removeAllRemoteUdpConnection();


    public abstract boolean createTcpConnection(String host, int port);
    public abstract void removeTcpConnection(int bindPort);
    public abstract void removeAllTcpConnection();

    public abstract boolean createUdpConnection(String host, int port);
    public abstract void removeUdpConnection(int bindPort);
    public abstract void removeAllUdpConnection();


    public abstract void destroyController();
}
