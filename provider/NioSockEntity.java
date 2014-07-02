package NioComponent.provider;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 * Created by charlown on 14-6-25.
 * rich blood entity
 */
public class NioSockEntity {
    public int channelType;  // TCP server/client UDP server/client
    public SocketChannel tcpChannel; //TCP client
    public ServerSocketChannel tcpChannelServer; //   (tcpChannel | udpChannel ) ∩ channelTcpServer = Φ
    public DatagramChannel udpChannel; //UDP client /server


    public int bindPort;
    public String host;
    public int port;

    private ByteArrayOutputStream outputStream;
    private ByteBuffer cache;
    public int bufferSize;

    public int step;
    public Object handle;






    public interface INioSockEventHandler {
        public void stillbirthSocket(NioSockEntity entity);
        public void birthSocket(NioSockEntity entity);
        public void deadSocket(NioSockEntity entity);
        public void birthBuffer(NioSockEntity entity);
    }

    public NioSockEntity()
    {
        cache = ByteBuffer.allocate(1024);
        outputStream = new ByteArrayOutputStream();
    }

    public NioSockEntity(int capacity)
    {
        cache = ByteBuffer.allocate(capacity);
        outputStream = new ByteArrayOutputStream();
    }

    public void reset()
    {
        channelType = -1;
        udpChannel = null;
        tcpChannel = null;
        tcpChannelServer  = null;

        bindPort = -1;
        host = "";
        handle = null;
        bufferSize = 0;

        outputStream.reset();
        cache.clear();

    }

    public void reset(Object handler)
    {
        channelType = -1;
        udpChannel = null;
        tcpChannel = null;
        tcpChannelServer  = null;

        bindPort = -1;
        host = "";

        handle = handler;
        bufferSize = 0;

        outputStream.reset();
        cache.clear();


    }

    public void setBuffer(ByteBuffer byteBuffer)
    {
        cache.put(byteBuffer);
        cache.flip();
        bufferSize = cache.remaining();
    }

    public byte[] getBuffer()
    {

        outputStream.write(cache.array(), 0, bufferSize);//specify offset and length, or not, toByeArray will be cache size, not bufferSize.

        return outputStream.toByteArray();
    }

    public void decodeSocketAddress(SocketChannel channel)
    {
        InetSocketAddress address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
        if (address != null) {
            host = address.getAddress().getHostAddress();
            //notify:  if as client  'bindPort' is allocate local port, 'port' is connect remote port.
            bindPort = channel.socket().getLocalPort();
            port = address.getPort();
        }
    }
    public void decodeSocketAddress(ServerSocketChannel channel)
    {
        bindPort = channel.socket().getLocalPort();
    }

    public void decodeSocketAddress(DatagramChannel channel)
    {
        InetSocketAddress address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
        if (address != null) {
            host = address.getAddress().getHostAddress();
            //notify:  if as client  'bindPort' is allocate local port, 'port' is connect remote port.
            bindPort = channel.socket().getLocalPort();
            port = address.getPort();
        }
    }

}
