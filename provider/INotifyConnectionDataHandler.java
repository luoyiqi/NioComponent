package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyConnectionDataHandler {
    public void notifyCreateConnection(int type, boolean isSuc, String host, int port, int allocatePort);
    public void notifyBindReceiveBuffer(int type, int bindPort, String from, int port, byte[] buffer, int bufferSize);
}
