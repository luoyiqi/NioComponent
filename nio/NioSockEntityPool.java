package nio;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by charlown on 14-6-25.
 */
public class NioSockEntityPool {
    private Queue<NioSockEntity> m_pool;

    public NioSockEntityPool(int capacity){
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            m_pool.offer(new NioSockEntity());
        }
    }
    public NioSockEntityPool(int capacity, ISockEventHandler sockEventHandler){
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            NioSockEntity arg = new NioSockEntity();
            arg.handle = sockEventHandler;
            m_pool.offer(arg);
        }
    }

    public NioSockEntityPool(int capacity, int bufferSize)
    {
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            m_pool.offer(new NioSockEntity(bufferSize));
        }
    }

    public NioSockEntityPool(int capacity, int bufferSize, ISockEventHandler sockEventHandler)
    {
        m_pool = new LinkedList<NioSockEntity>();

        for (int i = 0 ; i < capacity; i++)
        {
            NioSockEntity arg = new NioSockEntity(bufferSize);
            arg.handle = sockEventHandler;
            m_pool.offer(arg);
        }
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
        arg.reset();
        m_pool.offer(arg);
    }

    public void onDestroy()
    {
        m_pool.clear();
        m_pool = null;
    }


}
