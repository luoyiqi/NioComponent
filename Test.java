package NioComponent;

import NioComponent.provider.*;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {

    public static void main(String [] args)
    {

        INotifyServiceDataHandler serviceDataHandler = new INotifyServiceDataHandler() {
            @Override
            public void notifyRemoteReceiveBuffer(int bindPort, String host, int port, final  byte[] buffer, int bufferSize) {
                System.out.println("on port: " + bindPort + " receive client: " + host + ":" + port + ", data, data size = " + bufferSize);

            }
        };

        INotifyConnectionDataHandler connectionDataHandler = new INotifyConnectionDataHandler() {

            @Override
            public void notifyBindReceiveBuffer(int bindPort, String from, int port, byte[] buffer, int bufferSize) {
                System.out.println("on port: " + bindPort + " from client: " + from + ":" + port + ", data, data size = " + bufferSize);
            }
        };

        INotifyExceptionMsgHandler exceptionMsgHandler = new INotifyExceptionMsgHandler() {
            @Override
            public void notifyExceptionMsg(int type, int runtimeType, int subRuntimeType) {

            }
        };

        INotifyOperationStateHandler operationStateHandler = new INotifyOperationStateHandler() {
            @Override
            public void notifyOperationState(int type, int operationType, boolean isSuc) {

            }
        };


        NioSocketProvider nioSocketProvider = new NioSocketProvider();



        nioSocketProvider.addNotifyListener(serviceDataHandler);
        nioSocketProvider.addNotifyListener(connectionDataHandler);
        nioSocketProvider.addNotifyListener(exceptionMsgHandler);
        nioSocketProvider.addNotifyListener(operationStateHandler);

        nioSocketProvider.init();


        boolean isSuc;

        /*
        isSuc = nioSocketProvider.createServer(NioTypes.TYPE_UDP_SERVER, 10088);
        System.out.println("sever " + isSuc);
        isSuc = nioSocketProvider.createServer(NioTypes.TYPE_TCP_SERVER, 10087);
        System.out.println("sever " + isSuc);*/
        isSuc = nioSocketProvider.createConnection(NioTypes.TYPE_TCP_CLIENT, 10087, "192.168.3.8", 10088);


    }
}
