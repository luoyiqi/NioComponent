package NioComponent.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlown on 2014/6/27.
 */
public class NioSockMap<T> {

    private Map<String, T>  mSockMap;
    private int capacity = 1024;


    public NioSockMap()
    {
        mSockMap = new HashMap<String, T>(capacity);
    }

    public NioSockMap(int capacity)
    {
        this.capacity = capacity;
        mSockMap = new HashMap<String, T>(capacity);
    }


    public boolean addChannel(String key, T t)
    {
        boolean isSuc = false;

        if(!mSockMap.containsKey(key))
        {
            mSockMap.put(key, t);
            isSuc = true;
        }

        return isSuc;
    }


    public T updateChannel(String key, T newT)
    {
        T t = null;
        if (mSockMap.containsKey(key))
        {
            t = mSockMap.get(key);
            mSockMap.remove(key);
            mSockMap.put(key, newT);

        }

        return t;
    }

    public T removeChannel(String key)
    {
        T t = null;
        if (mSockMap.containsKey(key)) {
            t = mSockMap.get(key);
            mSockMap.remove(key);
        }

        return t;
    }

    public T getChannel(String key)
    {
        T t = null;
        if (mSockMap.containsKey(key)) {
            t = mSockMap.get(key);
        }

        return t;
    }

    public Collection<T> getChannels(){return  mSockMap.values();}

    public boolean isEmpty()
    {
        return mSockMap.isEmpty();
    }

    public int getSize(){return  mSockMap.size();}

    public void clear()
    {
        mSockMap.clear();
    }

}
