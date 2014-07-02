package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyServiceDataHandler {
    public void notifyRemoteReceiveBuffer(int type, int bindPort, String host, int port, byte[] buffer, int bufferSize);
}
