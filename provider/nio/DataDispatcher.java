package NioComponent.provider.nio;

import NioComponent.provider.INotifyConnectionDataHandler;
import NioComponent.provider.INotifyExceptionMsgHandler;
import NioComponent.provider.INotifyOperationStateHandler;
import NioComponent.provider.INotifyServiceDataHandler;

import java.util.Queue;

/**
 * Created by charlown on 2014/7/1.
 */
public class DataDispatcher extends Thread{
    public boolean isRun = true;

    public NioSockEntityPool mPool;
    public Queue<NioSockEntity> mBindTcpReceiveQueue;
    public Queue<NioSockEntity> mBindUdpReceiveQueue;
    public Queue<NioSockEntity> mRemoteTcpReceiveQueue;
    public Queue<NioSockEntity> mRemoteUdpReceiveQueue;


    public INotifyServiceDataHandler serviceDataEvent;
    public INotifyConnectionDataHandler connectionDataEvent;


    @Override
    public void run() {

        NioSockEntity entity;

        while (isRun)
        {
            if (mRemoteTcpReceiveQueue != null)
            {
                if (!mRemoteTcpReceiveQueue.isEmpty()) {
                    entity = mRemoteTcpReceiveQueue.poll();
                    if (entity != null && serviceDataEvent != null)
                    {
                        entity.dataBuffer.flip();
                        byte[] data = entity.dataBuffer.array();
                        serviceDataEvent.notifyRemoteReceiveBuffer(entity.bindPort, entity.host, entity.port, data, data.length);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }else if (mRemoteUdpReceiveQueue != null)
            {
                if (!mRemoteUdpReceiveQueue.isEmpty()) {
                    entity = mRemoteUdpReceiveQueue.poll();
                    if (entity != null && serviceDataEvent != null)
                    {
                        entity.dataBuffer.flip();
                        byte[] data = entity.dataBuffer.array();
                        serviceDataEvent.notifyRemoteReceiveBuffer(entity.bindPort, entity.host, entity.port, data, data.length);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
            else if (mBindTcpReceiveQueue != null)
            {
                if (!mBindTcpReceiveQueue.isEmpty()) {
                    entity = mBindTcpReceiveQueue.poll();
                    if (entity != null && connectionDataEvent != null)
                    {
                        entity.dataBuffer.flip();
                        byte[] data = entity.dataBuffer.array();
                        connectionDataEvent.notifyBindReceiveBuffer(entity.bindPort, data, data.length);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
            else if (mBindUdpReceiveQueue != null)
            {
                if (!mBindUdpReceiveQueue.isEmpty()) {
                    entity = mBindUdpReceiveQueue.poll();
                    if (entity != null && connectionDataEvent != null)
                    {
                        entity.dataBuffer.flip();
                        byte[] data = entity.dataBuffer.array();
                        connectionDataEvent.notifyBindReceiveBuffer(entity.port, data, data.length);
                        if (mPool != null)
                            mPool.recovery(entity);
                    }

                }

            }
        }
    }

}
