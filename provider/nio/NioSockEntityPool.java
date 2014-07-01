package NioComponent.provider.nio;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSockEntityPool {
    private Queue<NioSockEntity> m_pool;
    private Object back_handler;

    public NioSockEntityPool(int capacity){
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            m_pool.offer(new NioSockEntity());
        }
    }
    public NioSockEntityPool(int capacity, Object handler){
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            NioSockEntity arg = new NioSockEntity();
            arg.handle = handler;
            m_pool.offer(arg);
        }
        back_handler = handler;

    }

    public NioSockEntityPool(int capacity, int bufferSize)
    {
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            m_pool.offer(new NioSockEntity(bufferSize));
        }
    }

    public NioSockEntityPool(int capacity, int bufferSize, Object handler)
    {
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            NioSockEntity arg = new NioSockEntity(bufferSize);
            arg.handle = handler;
            m_pool.offer(arg);
        }

        back_handler = handler;
    }

    public NioSockEntity obtain()
    {
        NioSockEntity arg = null;

        if (!m_pool.isEmpty())
            arg = m_pool.poll();

        return arg;
    }

    public void recovery(NioSockEntity arg)
    {
        arg.reset(back_handler);
        m_pool.offer(arg);
    }

    public void onDestroy()
    {
        m_pool.clear();
        m_pool = null;
    }


}
