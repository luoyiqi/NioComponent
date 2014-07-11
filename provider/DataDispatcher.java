package NioComponent.provider;

import java.util.Queue;

/**
 * Created by charlown on 2014/7/1.
 */
public class DataDispatcher extends Thread {
    public boolean isRun = true;

    public NioSockEntityPool mPool;

    public Queue<NioSockEntity> mReceiveQueue;


    @Override
    public void run() {

        NioSockEntity entity;

        while (isRun) {
            if (mReceiveQueue != null) {
                if (!mReceiveQueue.isEmpty()) {
                    entity = mReceiveQueue.poll();
                    if (entity != null) {
                        byte[] data = entity.getBuffer();

                        switch (entity.channelType) {
                            case NioTypes.TYPE_TCP_SERVER:
                            case NioTypes.TYPE_UDP_SERVER: {
                                INotifyServiceEventHandler handler = (INotifyServiceEventHandler) entity.handle;
                                if (handler != null)
                                    handler.notifyRemoteReceiveBuffer(entity.channelType, entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                                break;
                            }
                            case NioTypes.TYPE_TCP_CLIENT:
                            case NioTypes.TYPE_UDP_CLIENT: {
                                INotifyConnectionEventHandler handler = (INotifyConnectionEventHandler) entity.handle;
                                if (handler != null)
                                    handler.notifyLocalReceiveBuffer(entity.channelType, entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                                break;
                            }
                        }
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
        }
    }

}
