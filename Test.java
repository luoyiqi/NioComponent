package NioComponent;

import NioComponent.provider.*;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {



    public static class TestCase
    {
        NioSocketProvider nioSocketProvider = new NioSocketProvider();
        int allocate_port;
        INotifyServiceDataHandler serviceDataHandler = new INotifyServiceDataHandler() {
            @Override
            public void notifyRemoteReceiveBuffer(int type, int bindPort, String host, int port, final  byte[] buffer, int bufferSize) {
             //   System.out.println("buffer size = " + buffer.length);
                String str = new String(buffer);
              //  System.out.println("on port: " + bindPort + " receive client: " + host + ":" + port + ", data: " + str +", data size = " + bufferSize);



                switch (type)
                {
                    case NioTypes.TYPE_TCP_SERVER:
                    {
                        try
                        {
                            SocketChannel channel =  nioSocketProvider.getRemoteTcpConnectionChannel(host, port);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize);
                        }catch (NullPointerException npe)
                        {
                            npe.printStackTrace();
                        }

                        break;
                    }
                    case NioTypes.TYPE_UDP_SERVER:
                    {
                        try
                        {
                            DatagramChannel channel =  nioSocketProvider.getUdpServiceChannel(bindPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize, host, port);
                        }catch (NullPointerException npe)
                        {
                            npe.printStackTrace();
                        }
                        break;
                    }
                }




            }
        };

        INotifyConnectionDataHandler connectionDataHandler = new INotifyConnectionDataHandler() {

            @Override
            public void notifyBindReceiveBuffer(int type, int bindPort, String from, int port, byte[] buffer, int bufferSize) {
             //   System.out.println("buffer size = " + buffer.length);
                String str = new String(buffer);
            //    System.out.println("on port: " + bindPort + " from client: " + from + ":" + port + ", data: "+ str +", data size = " + bufferSize);





                switch (type)
                {
                    case NioTypes.TYPE_TCP_CLIENT:
                    {
                        try
                        {
                            SocketChannel channel =  nioSocketProvider.getTcpConnectionChannel(bindPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize);
                        }catch (NullPointerException npe)
                        {
                            npe.printStackTrace();
                        }

                        break;
                    }
                    case NioTypes.TYPE_UDP_CLIENT:
                    {
                        try
                        {
                            DatagramChannel channel =  nioSocketProvider.getUdpConnectionChannel(bindPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize, from, port);
                        }catch (NullPointerException npe)
                        {
                            npe.printStackTrace();
                        }
                        break;
                    }
                }


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

            @Override
            public void notifyCreateConnection(int type, boolean isSuc, String host, int port, int allocatePort) {
                if (type == NioTypes.TYPE_TCP_CLIENT)
                {
                    if (isSuc)
                    {
                        allocate_port = allocatePort;
                        System.out.println("connection allocate port: " + allocate_port);
                    }
                }
            }
        };

        public TestCase()
        {
            nioSocketProvider.addNotifyListener(serviceDataHandler);
            nioSocketProvider.addNotifyListener(connectionDataHandler);
            nioSocketProvider.addNotifyListener(exceptionMsgHandler);
            nioSocketProvider.addNotifyListener(operationStateHandler);
        }



        public void start()
        {
            nioSocketProvider.init();

           nioSocketProvider.createConnection(NioTypes.TYPE_TCP_CLIENT, "192.168.3.8", 10088);
          //  nioSocketProvider.createServer(NioTypes.TYPE_TCP_SERVER, 10089);
           // nioSocketProvider.createServer(NioTypes.TYPE_UDP_SERVER, 10090);
           // nioSocketProvider.createConnection(NioTypes.TYPE_UDP_CLIENT, 10092, "192.168.3.8", 10091);
        }


    }



    public static void main(String [] args)
    {


        TestCase testCase = new TestCase();
        testCase.start();

    }
}
