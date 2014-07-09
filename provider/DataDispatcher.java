package NioComponent.provider;

import java.util.Queue;

/**
 * Created by charlown on 2014/7/1.
 */
public class DataDispatcher extends Thread {
    public boolean isRun = true;

    public NioSockEntityPool mPool;
    public Queue<NioSockEntity> mBindTcpReceiveQueue;
    public Queue<NioSockEntity> mBindUdpReceiveQueue;
    public Queue<NioSockEntity> mRemoteTcpReceiveQueue;
    public Queue<NioSockEntity> mRemoteUdpReceiveQueue;





    @Override
    public void run() {

        NioSockEntity entity;

        while (isRun) {
            if (mRemoteTcpReceiveQueue != null) {
                if (!mRemoteTcpReceiveQueue.isEmpty()) {
                    entity = mRemoteTcpReceiveQueue.poll();
                    if (entity != null ) {
                        byte[] data = entity.getBuffer();
                        INotifyServiceDataHandler handler = (INotifyServiceDataHandler)entity.handle;
                        if (handler != null)
                            handler.notifyRemoteReceiveBuffer(entity.channelType ,entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
            if (mRemoteUdpReceiveQueue != null) {
                if (!mRemoteUdpReceiveQueue.isEmpty()) {
                    entity = mRemoteUdpReceiveQueue.poll();
                    if (entity != null) {
                        byte[] data = entity.getBuffer();
                        INotifyServiceDataHandler handler = (INotifyServiceDataHandler)entity.handle;
                        if (handler != null)
                            handler.notifyRemoteReceiveBuffer(entity.channelType ,entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }

            if (mBindTcpReceiveQueue != null) {
                if (!mBindTcpReceiveQueue.isEmpty()) {
                    entity = mBindTcpReceiveQueue.poll();
                    if (entity != null) {

                        byte[] data = entity.getBuffer();
                        INotifyConnectionDataHandler handler = (INotifyConnectionDataHandler)entity.handle;
                        if (handler != null)
                            handler.notifyBindReceiveBuffer(entity.channelType ,entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }

            if (mBindUdpReceiveQueue != null) {
                if (!mBindUdpReceiveQueue.isEmpty()) {
                    entity = mBindUdpReceiveQueue.poll();
                    if (entity != null) {
                        byte[] data = entity.getBuffer();
                        INotifyConnectionDataHandler handler = (INotifyConnectionDataHandler)entity.handle;
                        if (handler != null)
                            handler.notifyBindReceiveBuffer(entity.channelType ,entity.bindPort, entity.host, entity.port, data, entity.bufferSize);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
        }
    }

}
