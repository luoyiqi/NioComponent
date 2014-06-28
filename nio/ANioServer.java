package NioComponent.nio;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by charlown on 2014/6/27.
 */
public abstract class ANioServer<T> {
    protected int capacity = 1024;
    protected int poolCapacity = 6 * 1024;//server:1024,client:1024,buffer:4096
    protected NioSockMap<T>  mServerMap;
    protected NioSockMap<SocketChannel> mClientMap;
    protected NioSockEntityPool mPool;
    protected Queue<NioSockEntity> mReceiveQueue;
    protected ISockNotifyEventHandler notifyEvent = null;
    protected Selector mSelector;

    public abstract void init();
    public abstract boolean createServer(int bindPort);
    public abstract void removeServer(int bindPort);
    public abstract void removeAllServer();
    public abstract void removeClient(int bindPort, String ip, int port);
    public abstract void removeAllClient();
    public abstract ArrayList<String> getClientList();
    public abstract void onDestroy();
    public void addNotifyHandler(ISockNotifyEventHandler eventHandler)
    {
        notifyEvent = eventHandler;
    }



}
