package NioComponent.nio;

import java.nio.channels.Selector;
import java.util.Queue;

/**
 * Created by charlown on 2014/6/30.
 */
public abstract class ANioController implements NioSockEntity.INioSockEventHandler {
    protected int capacity = 1024;
    protected int poolCapacity = 6 * 1024;//server:1024,client:1024,buffer:4096

    protected NioSockMap<NioSockEntity> mBindSocks;
    protected NioSockMap<NioSockEntity> mRemoteSocks;
    protected NioSockEntityPool mPool;
    protected Queue<NioSockEntity> mReceiveQueue;

    protected Selector mSelector;



    protected INioSockNotifyEventHandler notifyEvent = null;
    public void addNotifyHandler(INioSockNotifyEventHandler eventHandler) {
        notifyEvent = eventHandler;
    }



    public abstract void init();


    public abstract boolean createTcpService(int bindPort);
    public abstract void removeTcpService(int bindPort);
    public abstract void removeAllTcpService();

    public abstract boolean createUdpService(int bindPort);
    public abstract void removeUdpService(int bindPort);
    public abstract void removeAllUdpService();

    public abstract void removeRemoteConnection(String ip, int port);
    public abstract void removeAllRemoteConnection();

    public abstract boolean createConnection(String host, int port);
    public abstract void removeLocalConnection(int bindPort);
    public abstract void  removeAllConnection();


    public abstract void destroyController();
}
