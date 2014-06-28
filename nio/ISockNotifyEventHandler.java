package NioComponent.nio;

/**
 * Created by charlown on 2014/6/27.
 */
public interface ISockNotifyEventHandler {
    public void notifyServerOperationState(boolean state, int action, int port);
    public void notifyClientOperationState(boolean state, int action, int bindPort, String ip, int port);
    public void notifyReceiveBufferQueue(int bindPort, String ip, int port);
}
