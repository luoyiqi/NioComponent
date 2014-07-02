package NioComponent.provider;


/**
 * Created by charlown on 14-6-25.
 */
public class NioSocketProvider {

    private NioSockController controller;

    private INotifyServiceDataHandler serviceDataHandler = null;
    private INotifyConnectionDataHandler connectionDataHandler = null;
    private INotifyOperationStateHandler operationStateHandler = null;
    private INotifyExceptionMsgHandler exceptionMsgHandler = null;


    public void addNotifyListener(INotifyServiceDataHandler notifyServiceData) {
        serviceDataHandler = notifyServiceData;
    }

    public void addNotifyListener(INotifyConnectionDataHandler notifyConnectionData) {
        connectionDataHandler = notifyConnectionData;
    }

    public void addNotifyListener(INotifyOperationStateHandler notifyOperationState) {
        operationStateHandler = notifyOperationState;
    }

    public void addNotifyListener(INotifyExceptionMsgHandler notifyExceptionMsg) {
        exceptionMsgHandler = notifyExceptionMsg;
    }


    public void init() throws NullPointerException {

        if (serviceDataHandler == null)
            throw new NullPointerException("NotifyServiceDataListener is null.");

        if (connectionDataHandler == null)
            throw new NullPointerException("NotifyConnectionDataListener is null.");

        if (exceptionMsgHandler == null)
            throw new NullPointerException("NotifyExceptionMsgListener is null.");

        if (operationStateHandler == null)
            throw new NullPointerException("NotifyOperationStateListener is null.");

        if (controller == null) {
            controller = new NioSockController();

            controller.addNotifyHandler(serviceDataHandler);
            controller.addNotifyHandler(connectionDataHandler);
            controller.addNotifyHandler(exceptionMsgHandler);
            controller.addNotifyHandler(operationStateHandler);


            controller.init();
        }

    }


    public boolean createServer(int type, int port) {
        boolean isSuc = false;


        if (controller != null) {
            if (type == NioTypes.TYPE_TCP_SERVER) {
                isSuc = controller.createTcpService(port);
            } else if (type == NioTypes.TYPE_UDP_SERVER) {
                isSuc = controller.createUdpService(port);
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
        } else if (type == NioTypes.TYPE_UDP_CLIENT) {
            if (controller != null) {
                controller.removeRemoteUdpConnection(host, port);
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


    public boolean createConnection(int type, String host, int port) {
        boolean isSuc = false;


        if (controller != null) {

            if (type == NioTypes.TYPE_TCP_CLIENT) {
                isSuc = controller.createTcpConnection(host, port);
            } else if (type == NioTypes.TYPE_UDP_CLIENT) {
                isSuc = controller.createUdpConnection(host, port);
            }

        }

        return isSuc;
    }

    public boolean createConnection(int type, int bindPort, String host, int port) {
        boolean isSuc = false;


        if (controller != null) {

            if (type == NioTypes.TYPE_TCP_CLIENT) {
                isSuc = controller.createTcpConnection(bindPort, host, port);
            } else if (type == NioTypes.TYPE_UDP_CLIENT) {
                isSuc = controller.createUdpConnection(bindPort, host, port);
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


    public void destroyController() {
        if (controller != null) {
            controller.destroyController();
        }
    }


}
