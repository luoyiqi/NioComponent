package NioComponent.provider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

/**
 * Created by charlown on 2014/7/2.
 */
public class NioSockSender extends Thread{
    public boolean isRun = true;
    public Queue<NioSockEntity> sendCache;
    public NioSockEntityPool mPool;

    @Override
    public void run() {

        while (isRun)
        {
            if (sendCache != null)
            {
                if (!sendCache.isEmpty())
                {
                    NioSockEntity sendEntity = sendCache.poll();

                    if (sendEntity != null) {

                        switch (sendEntity.channelType) {
                            case NioTypes.TYPE_TCP_SERVER:
                            case NioTypes.TYPE_TCP_CLIENT: {
                                try {
                                    sendEntity.tcpChannel.write(sendEntity.getSendByteBuffer());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case NioTypes.TYPE_UDP_SERVER:
                            case NioTypes.TYPE_UDP_CLIENT: {
                                try {
                                    sendEntity.udpChannel.send(sendEntity.getSendByteBuffer(), new InetSocketAddress(sendEntity.host, sendEntity.port));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }

                        mPool.recovery(sendEntity);
                    }

                }else
                {
                    try {
                        sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
