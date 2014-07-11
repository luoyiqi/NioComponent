package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyServiceEventHandler {
    public void notifyCreateRemoteConnection(int type, int bindPort, String host, int port);
    public void notifyRemoteDisconnect(int type, int bindPort, String host, int port);
    public void notifyRemoteReceiveBuffer(int type, int bindPort, String host, int port, byte[] buffer, int bufferSize);
}
