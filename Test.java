package NioComponent;

import NioComponent.nio.INioSockNotifyEventHandler;
import NioComponent.nio.NioSocketProvider;
import NioComponent.nio.NioTypes;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {

    public static void main(String [] args)
    {


        INioSockNotifyEventHandler notifyEventHandler = new INioSockNotifyEventHandler() {
            @Override
            public void notifyServerOperationState(boolean state, int action, int port) {
                System.out.println("server op,  state, action, port = " + state +", " + action + ", " + port);
            }

            @Override
            public void notifyClientOperationState(boolean state, int action, int bindPort, String ip, int port) {
                System.out.println("client op,  state, action, bindPort, ip, port = " + state +", " + action + ", " + bindPort +", " + ip +", "+ port);
            }

            @Override
            public void notifyReceiveBufferQueue(int bindPort, String ip, int port) {
                System.out.println("receive buffer, bindPort, ip, port = " + bindPort + ", "+ ip +", " + port);

            }
        };


        NioSocketProvider nioSocketProvider = new NioSocketProvider();
        nioSocketProvider.initServer(NioTypes.TYPE_TCP_SERVER);
        nioSocketProvider.addServerNotifyListener(notifyEventHandler);

        boolean isSuc;
        isSuc = nioSocketProvider.createServer(NioTypes.TYPE_TCP_SERVER, 10086);
        System.out.println(isSuc);

    }
}