package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyConnectionEventHandler {
    public void notifyCreateConnection(int type, int localPort, String remoteIp, int remotePort);
    public void notifyDisconnect(int type, int localPort, String remoteIp, int remotePort);
    public void notifyLocalReceiveBuffer(int type, int localPort, String remoteIp, int remotePort, byte[] buffer, int bufferSize);
}
