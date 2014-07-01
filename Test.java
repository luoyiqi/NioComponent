package NioComponent;

import NioComponent.provider.INotifyConnectionDataHandler;
import NioComponent.provider.INotifyServiceDataHandler;
import NioComponent.provider.NioSocketProvider;
import NioComponent.provider.NioTypes;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {

    public static void main(String [] args)
    {

        INotifyServiceDataHandler serviceDataHandler = new INotifyServiceDataHandler() {
            @Override
            public void notifyRemoteReceiveBuffer(int bindPort, String host, int port, byte[] buffer, int bufferSize) {
                System.out.println("on port: " + bindPort + "receive client: " + host + ":" + port + ", data, data size = " + bufferSize);

            }
        };

        INotifyConnectionDataHandler connectionDataHandler = new INotifyConnectionDataHandler() {
            @Override
            public void notifyBindReceiveBuffer(int bindPort, byte[] buffer, int bufferSize) {

                System.out.println("on port: " + bindPort + "receive data, size = " + bufferSize);
            }
        };


        NioSocketProvider nioSocketProvider = new NioSocketProvider();



        nioSocketProvider.addNotifyListener(serviceDataHandler);
        nioSocketProvider.addNotifyListener(connectionDataHandler);

        nioSocketProvider.init();


        boolean isSuc;
        isSuc = nioSocketProvider.createServer(NioTypes.TYPE_TCP_SERVER, 10087);
        System.out.println(isSuc);
        isSuc = nioSocketProvider.createConnection(NioTypes.TYPE_TCP_CLIENT, "192.168.3.8", 10086);
        System.out.println(isSuc);


    }
}
