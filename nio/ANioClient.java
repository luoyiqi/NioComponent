package NioComponent.nio;

/**
 * Created by charlown on 2014/6/27.
 */
public abstract class ANioClient {
    public abstract void init();
    public abstract boolean connectServer(String host, int port);
}
