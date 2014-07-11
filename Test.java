package NioComponent;

import NioComponent.provider.*;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {


    public static class TestCase {
        NioSocketProvider nioSocketProvider = new NioSocketProvider();
        int allocate_port;

        public static ArrayList<Integer> ports = new ArrayList<Integer>();
        INotifyServiceEventHandler serviceDataHandler = new INotifyServiceEventHandler() {

            @Override
            public void notifyCreateRemoteConnection(int type, int bindPort, String host, int port) {
                System.out.println("on port: " + bindPort + " remote: " + host + ":" + port + " connect");
            }

            @Override
            public void notifyRemoteDisconnect(int type, int bindPort, String host, int port) {

                System.out.println("on port: " + bindPort + " remote: " + host + ":" + port + " disconnect");
            }

            @Override
            public void notifyRemoteReceiveBuffer(int type, int bindPort, String host, int port, final byte[] buffer, int bufferSize) {
                //   System.out.println("buffer size = " + buffer.length);
               // String str = new String(buffer);
                  System.out.println("on port: " + bindPort + " receive client: " + host + ":" + port + ", data size = " + bufferSize);


                switch (type) {
                    case NioTypes.TYPE_TCP_SERVER: {
                        try {
                            SocketChannel channel = nioSocketProvider.getRemoteTcpConnectionChannel(host, port);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }

                        break;
                    }
                    case NioTypes.TYPE_UDP_SERVER: {
                        try {
                            DatagramChannel channel = nioSocketProvider.getUdpServiceChannel(bindPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize, host, port);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                        break;
                    }
                }


            }
        };

        INotifyConnectionEventHandler connectionDataHandler = new INotifyConnectionEventHandler() {

            @Override
            public void notifyCreateConnection(int type, int localPort, String remoteIp, int remotePort) {
                System.out.println("on port: " + localPort + " connect: " + remoteIp + ":" + remotePort + " success");
            }

            @Override
            public void notifyDisconnect(int type, int localPort, String remoteIp, int remotePort) {
                System.out.println("on port: " + localPort + " connect: " + remoteIp + ":" + remotePort + " fail");
            }

            @Override
            public void notifyLocalReceiveBuffer(int type, int localPort, String remoteIp, int remotePort, byte[] buffer, int bufferSize) {
                switch (type) {
                    case NioTypes.TYPE_TCP_CLIENT: {
                        try {
                            SocketChannel channel = nioSocketProvider.getTcpConnectionChannel(localPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }

                        break;
                    }
                    case NioTypes.TYPE_UDP_CLIENT: {
                        try {
                            DatagramChannel channel = nioSocketProvider.getUdpConnectionChannel(localPort);

                            nioSocketProvider.addBufferToSend(type, channel, buffer, bufferSize, remoteIp, remotePort);
                        } catch (NullPointerException npe) {
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


        public TestCase() {

            nioSocketProvider.addNotifyListener(exceptionMsgHandler);

        }


        public void start() {
            nioSocketProvider.init();

              nioSocketProvider.createConnection(NioTypes.TYPE_TCP_CLIENT, "192.168.3.8", 10088, connectionDataHandler);
              nioSocketProvider.createServer(NioTypes.TYPE_TCP_SERVER, 10089, serviceDataHandler);
            //   nioSocketProvider.createServer(NioTypes.TYPE_UDP_SERVER, 10090, serviceDataHandler);
           // nioSocketProvider.createConnection(NioTypes.TYPE_UDP_CLIENT, 10092, "192.168.3.8", 10091, connectionDataHandler);


        }


    }


    public static void main(String[] args) {


        TestCase testCase = new TestCase();
        testCase.start();

    }
}
