package NioComponent.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by charlown on 2014/6/27.
 *
 */
public class NioTcpClient extends ANioClient implements INioSockEventHandler{

    protected int capacity = 1024;
    protected int poolCapacity = 6 * 1024;//server:1024,client:1024,buffer:4096
    protected NioSockMap<SocketChannel> mClientMap;
    protected NioSockEntityPool mPool;
    protected Queue<NioSockEntity> mReceiveQueue;
    protected INioSockNotifyEventHandler notifyEvent = null;
    protected Selector mSelector;



    public NioTcpClient()
    {
        super();
        mClientMap = new NioSockMap<SocketChannel>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }


    public NioTcpClient(int capacity, int poolCapacity)
    {
        super();
        this.capacity = capacity;
        this.poolCapacity = poolCapacity;
        mClientMap = new NioSockMap<SocketChannel>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }



    @Override
    public void init() {

    }

    @Override
    public boolean connectServer(String host, int port) {

        boolean isSuc = false;

        try
        {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
            channel.configureBlocking(false);
            String key = host  + ":" + port;
            isSuc = mClientMap.addChannel(key, channel);
            mSelector.wakeup();
            channel.register(mSelector, SelectionKey.OP_CONNECT, this);

        }catch (IOException ie)
        {

        }

        return isSuc;
    }

    @Override
    public void addClientMap(NioSockEntity arg) {
        //
    }

    @Override
    public void addReceiveBufferQueue(NioSockEntity arg) {

    }

    @Override
    public void removeClient(int bindPort, String ip, int port) {

    }
}
