package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyServiceDataHandler {
    public void notifyCreateRemoteConnection(int type, boolean isSuc, String host, int port, int allocatePort);
    public void notifyRemoteReceiveBuffer(int type, int bindPort, String host, int port, byte[] buffer, int bufferSize);
}
