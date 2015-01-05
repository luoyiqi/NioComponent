package NioComponent.provider;


import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSocketProvider {

    private NioSockController controller;

    private INotifyExceptionMsgHandler exceptionMsgHandler = null;


    public void addNotifyListener(INotifyExceptionMsgHandler notifyExceptionMsg) {
        exceptionMsgHandler = notifyExceptionMsg;
    }


    private INotifyExceptionMsgHandler provider_NotifyExceptionMsgHandler = new INotifyExceptionMsgHandler() {
        @Override
        public void notifyExceptionMsg(int type, int runtimeType, int subRuntimeType) {
            if (exceptionMsgHandler != null)
                exceptionMsgHandler.notifyExceptionMsg(type, runtimeType, subRuntimeType);
        }
    };


    public void init() {

        if (controller == null) {
            controller = new NioSockController(1024*10, 1024, 1024, 2048);

            controller.addNotifyHandler(provider_NotifyExceptionMsgHandler);

            controller.init(2048);
        }

    }


    public boolean createServer(int type, int port, INotifyServiceEventHandler handler) {
        boolean isSuc = false;


        if (controller != null) {
            if (type == NioTypes.TYPE_TCP_SERVER) {
                isSuc = controller.createTcpService(port, handler);
            } else if (type == NioTypes.TYPE_UDP_SERVER) {
                isSuc = controller.createUdpService(port, handler);
            }
        }

        return isSuc;
    }


    public void stopServer(int type, int port) {

        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (controller != null) {
                controller.removeTcpService(port);
            }
        } else if (type == NioTypes.TYPE_UDP_SERVER) {
            if (controller != null) {
                controller.removeUdpService(port);
            }
        }

    }

    public void stopAllServer(int type) {
        if (type == NioTypes.TYPE_TCP_SERVER) {
            if (controller != null) {
                controller.removeAllTcpService();
            }
        } else if (type == NioTypes.TYPE_UDP_SERVER) {
            if (controller != null) {
                controller.removeAllUdpService();
            }
        }
    }


    public void stopRemoteConnection(int type, String host, int port) {
        if (type == NioTypes.TYPE_TCP_CLIENT) {
            if (controller != null) {
                controller.removeRemoteTcpConnection(host, port);
            }
        }
    }

    public void stopAllRemoteConnection(int type) {
        if (type == NioTypes.TYPE_TCP_CLIENT) {
            if (controller != null) {
                controller.removeAllRemoteTcpConnection();
            }
        } else if (type == NioTypes.TYPE_UDP_CLIENT) {
            if (controller != null) {
                controller.removeAllRemoteTcpConnection();
            }
        }
    }


    public boolean createConnection(int type, String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;


        if (controller != null) {

            if (type == NioTypes.TYPE_TCP_CLIENT) {
                isSuc = controller.createTcpConnection(host, port, handler);
            } else if (type == NioTypes.TYPE_UDP_CLIENT) {
                isSuc = controller.createUdpConnection(host, port, handler);
            }

        }

        return isSuc;
    }

    public boolean createConnection(int type, int bindPort, String host, int port, INotifyConnectionEventHandler handler) {
        boolean isSuc = false;


        if (controller != null) {

            if (type == NioTypes.TYPE_TCP_CLIENT) {
                isSuc = controller.createTcpConnection(bindPort, host, port, handler);
            } else if (type == NioTypes.TYPE_UDP_CLIENT) {
                isSuc = controller.createUdpConnection(bindPort, host, port, handler);
            }

        }

        return isSuc;
    }


    public void stopConnection(int type, int bindPort) {
        if (type == NioTypes.TYPE_TCP_CLIENT) {
            if (controller != null) {
                controller.removeTcpConnection(bindPort);
            }
        } else if (type == NioTypes.TYPE_UDP_CLIENT) {
            if (controller != null) {
                controller.removeUdpConnection(bindPort);
            }
        }
    }

    public void stopAllConnection(int type) {
        if (type == NioTypes.TYPE_TCP_CLIENT) {
            if (controller != null) {
                controller.removeAllTcpConnection();
            }
        } else if (type == NioTypes.TYPE_UDP_CLIENT) {
            if (controller != null) {
                controller.removeAllUdpConnection();
            }
        }
    }


    public SocketChannel getTcpConnectionChannel(int bindPort) {

        SocketChannel channel = null;


        if (controller != null) {
            channel = controller.getConnectionSocketChannel(bindPort);
        }


        return channel;

    }

    public DatagramChannel getUdpConnectionChannel(int bindPort) {

        DatagramChannel channel = null;


        if (controller != null) {
            channel = controller.getConnectionDatagramChannel(bindPort);
        }

        return channel;

    }

    public SocketChannel getRemoteTcpConnectionChannel(String host, int port) {

        SocketChannel channel = null;


        if (controller != null) {
            channel = controller.getRemoteConnectionSocketChannel(host, port);
        }


        return channel;

    }

    public DatagramChannel getUdpServiceChannel(int bindPort) {

        DatagramChannel channel = null;


        if (controller != null) {
            channel = controller.getServiceDatagramChannel(bindPort);
        }


        return channel;

    }


    public void addBufferToSend(int type, SocketChannel channel, byte[] data, int dataSize) {
        if (controller != null)
            controller.addBufferToSend(type, channel, data, dataSize);
    }

    public void addBufferToSend(int type, DatagramChannel channel, byte[] data, int dataSize, String host, int port) {

        if (controller != null)
            controller.addBufferToSend(type, channel, data, dataSize, host, port);
    }


    public void destroyController() {
        if (controller != null) {
            controller.destroyController();
        }
    }

}
