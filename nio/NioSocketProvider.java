package NioComponent.nio;


import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSocketProvider {


    private final Lock objLock = new ReentrantLock();

    private ANioServer<ServerSocketChannel> mTcpServer;


    private INioSockNotifyEventHandler serverNotifyEvent;
    private INioSockNotifyEventHandler clientNotifyEvent;

    public void addServerNotifyListener(INioSockNotifyEventHandler notifyEventHandler)
    {
        serverNotifyEvent = notifyEventHandler;

        if (mTcpServer != null)
        {
            mTcpServer.addNotifyHandler(serverNotifyEvent);
        }
        //else if ()
    }
    public void addClientNotifyListener(INioSockNotifyEventHandler notifyEventHandler)
    {
        clientNotifyEvent = notifyEventHandler;
        //if ()
    }

    public void initServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (mTcpServer == null) {
                mTcpServer = new NioTcpServer();
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
