package nio;

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

/**
 * Created by charlown on 2014/6/27.
 *
 */
public class NioTcpServer extends ANioServer<ServerSocketChannel> implements ISockEventHandler{

    public NioTcpServer()
    {
        super();
        mServerMap = new NioSockMap<ServerSocketChannel>(capacity);
        mClientMap = new NioSockMap<SocketChannel>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }
    public NioTcpServer(int capacity, int poolCapacity)
    {
        super();
        this.capacity = capacity;
        this.poolCapacity = poolCapacity;
        mServerMap = new NioSockMap<ServerSocketChannel>(capacity);
        mClientMap = new NioSockMap<SocketChannel>(capacity);
        mPool = new NioSockEntityPool(poolCapacity, this);
        mReceiveQueue = new LinkedList<NioSockEntity>();
    }

    private NioSockThread nioSockThread = null;


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
    public boolean createServer(int bindPort) {

        boolean isSuc = false;


        try {




            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


            serverSocketChannel.socket().bind(new InetSocketAddress(bindPort));
            serverSocketChannel.configureBlocking(false);

            mSelector.wakeup();
            SelectionKey selectionKey = serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);


            String key = serverSocketChannel.socket().getLocalPort()+"";

            isSuc = mServerMap.addChannel(key, serverSocketChannel);


            if (notifyEvent != null)
             {
                 notifyEvent.notifyServerOperationState(isSuc, NioTypes.ACTION_ADD_TCP_SERVER, serverSocketChannel.socket().getLocalPort());
             }

        }
        catch (BindException be)
        {
            //
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        return isSuc;
    }

    @Override
    public void removeServer(int bindPort) {
        String key = bindPort +"";

        ServerSocketChannel channel =  mServerMap.removeChannel(key);
        if (channel != null)
        {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeAllServer() {

        if (!mServerMap.isEmpty())
        {
            Collection<ServerSocketChannel> serverSocketChannels = mServerMap.getChannels();

            for (ServerSocketChannel channel:serverSocketChannels)
            {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mServerMap.clear();
        }

    }

    @Override
    public void removeClient(int bindPort, String ip, int port) {
        String key = ip + ":" + port;

        SocketChannel channel = mClientMap.removeChannel(key);
        if (channel != null)
        {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (notifyEvent != null)
        {
            notifyEvent.notifyClientOperationState(true, NioTypes.ACTION_REMOVE_TCP_CLIENT, 0, ip, port);
        }

    }

    @Override
    public void removeAllClient() {
        if (!mClientMap.isEmpty())
        {
            Collection<SocketChannel> socketChannels = mClientMap.getChannels();

            for (SocketChannel channel:socketChannels)
            {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mClientMap.clear();
        }

    }

    @Override
    public ArrayList<String> getClientList() {
        return null;
    }

    @Override
    public void onDestroy() {
        if (nioSockThread != null)
        {
            nioSockThread.isRun = false;
        }

        removeAllServer();
        removeAllClient();

        mPool.onDestroy();

        if (mSelector != null)
        {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void addClientMap(NioSockEntity arg) {
        //check?
        String key = arg.host + ":" + arg.port;
        boolean isSuc;

        isSuc = mClientMap.addChannel(key, arg.channel);

        if (notifyEvent != null)
        {
            notifyEvent.notifyClientOperationState(isSuc,NioTypes.ACTION_ADD_TCP_CLIENT, arg.bindPort, arg.host, arg.port);
        }

    }



    @Override
    public void addReceiveBufferQueue(NioSockEntity arg) {
        mReceiveQueue.add(arg);

        if (notifyEvent != null)
        {
            notifyEvent.notifyReceiveBufferQueue(arg.bindPort, arg.host, arg.port);
        }

    }





}
