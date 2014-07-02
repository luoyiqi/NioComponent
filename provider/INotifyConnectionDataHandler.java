package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyConnectionDataHandler {
    public void notifyBindReceiveBuffer(int type, int bindPort, String from, int port, byte[] buffer, int bufferSize);
}
