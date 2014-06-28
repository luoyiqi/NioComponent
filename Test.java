package NioComponent;

import NioComponent.nio.NioSockManager;
import NioComponent.nio.NioTypes;

/**
 * Created by charlown on 2014/6/27.
 */
public class Test {

    public static void main(String [] args)
    {


        NioSockManager nioSockManager = new NioSockManager();
        nioSockManager.initServer(NioTypes.TYPE_TCP_SERVER);
        boolean isSuc;
        isSuc = nioSockManager.createServer(NioTypes.TYPE_TCP_SERVER, 10086);
        System.out.println(isSuc);

    }
}
