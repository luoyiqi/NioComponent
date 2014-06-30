package NioComponent.nio;


import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSocketProvider {

    private NioSockController controller;



    public void addNotifyListener(INioSockNotifyEventHandler notifyEventHandler)
    {

       if (controller != null)
       {
           controller.addNotifyHandler(notifyEventHandler);
       }
    }


    public void init() {

        if (controller == null)
        {
            controller = new NioSockController();
            controller.init();
        }

    }



    public boolean createServer(int type, int port) {
        boolean isSuc = false;

        if (type == NioTypes.TYPE_TCP_SERVER)
        {
            if (controller != null)
            {
                isSuc = controller.createTcpService(port);
            }
        }
        else if (type == NioTypes.TYPE_UDP_SERVER)
        {
            if (controller != null)
            {
                isSuc = controller.createUdpService(port);
            }
        }

        return isSuc;
    }


    public boolean createClient(int type, String host, int port) {
        boolean isSuc = false;


        if (controller != null)
        {
           isSuc = controller.createConnection(type, host, port);
        }


        return isSuc;
    }

    public void stopServer(int type, int port) {

        if (type == NioTypes.TYPE_TCP_SERVER)
        {
            if (controller != null)
            {
                controller.removeTcpService(port);
            }
        }
        else if (type == NioTypes.TYPE_UDP_SERVER)
        {
            if (controller != null)
            {
                controller.removeUdpService(port);
            }
        }

    }

    public void stopAllServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER)
        {
            if (controller != null)
            {
                controller.removeAllTcpService();
            }
        }
        else if (type == NioTypes.TYPE_UDP_SERVER)
        {
            if (controller != null)
            {
                controller.removeAllUdpService();
            }
        }
    }

    public void destroyController()
    {
        if (controller != null)
        {
            controller.destroyController();
        }
    }


}
