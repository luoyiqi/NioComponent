package NioComponent.nio;

/**
 * Created by charlown on 2014/6/27.
 */
public interface ISockEventHandler {
    public void addClientMap(NioSockEntity arg);
    public void addReceiveBufferQueue(NioSockEntity arg);
    public void removeClient(int bindPort, String ip, int port);
}
