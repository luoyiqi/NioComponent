package nio;


import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSockManager {


    private final Lock objLock = new ReentrantLock();

    private ANioServer<ServerSocketChannel> mTcpServer;

    ISockNotifyEventHandler notifyEventHandler = new ISockNotifyEventHandler() {

        @Override
        public void notifyServerOperationState(boolean state, int action, int port) {
            switch (action) {
                case NioTypes.ACTION_ADD_TCP_SERVER: {
                    System.out.println("create server on port: " + port + ", operation add." + "state: " + state);
                    break;
                }
                case NioTypes.ACTION_REMOVE_TCP_SERVER: {
                    System.out.println("create server on port: " + port + ", operation remove.");
                    break;
                }
            }

        }

        @Override
        public void notifyClientOperationState(boolean state, int action, int bindPort, String ip, int port) {
            switch (action) {
                case NioTypes.ACTION_ADD_TCP_CLIENT: {
                    System.out.println("on local port: " + bindPort + "from client:" + ip + ":" + port + ", operation add.");
                    break;
                }
                case NioTypes.ACTION_REMOVE_TCP_CLIENT: {
                    System.out.println("on local port: " + bindPort + "from client:" + ip + ":" + port + ", operation remove.");
                    System.out.println("clients size = " + mTcpServer.mClientMap.getSize());
                    break;
                }
            }

        }


        @Override
        public void notifyReceiveBufferQueue(int bindPort, String ip, int port) {
            System.out.println("on local port" + bindPort + " have data from " + ip + ":" + port);
        }
    };


    public void initServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (mTcpServer == null) {
                mTcpServer = new NioTcpServer();
                mTcpServer.addNotifyHandler(notifyEventHandler);
                mTcpServer.init();
            }

        } else if (type == NioTypes.TYPE_UDP_SERVER) {

        }
    }

    public boolean createServer(int type, int port) {
        boolean isSuc = false;

        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (mTcpServer != null) {
                isSuc = mTcpServer.createServer(port);
            }

        } else if (type == NioTypes.TYPE_UDP_SERVER) {

        }

        return isSuc;
    }


    public boolean createClient(int type, String host, int port) {
        boolean isSuc = false;


        if (type == NioTypes.TYPE_TCP_CLIENT) {

        } else if (type == NioTypes.TYPE_UDP_CLIENT) {

        }


        return isSuc;
    }

    public void stopServer(int type, int port) {
        try {
            objLock.tryLock(1, TimeUnit.SECONDS);

            if (type == NioTypes.TYPE_TCP_SERVER) {
                if (mTcpServer != null) {
                    mTcpServer.removeServer(port);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objLock.unlock();
        }
    }

    public void stopAllServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (mTcpServer != null) {
                mTcpServer.removeAllServer();
            }
        }
    }

    public void destroyServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (mTcpServer != null) {
                mTcpServer.onDestroy();
            }
        }
    }


}
