package NioComponent.provider.nio;


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

    public ByteBuffer dataBuffer;

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

        dataBuffer = ByteBuffer.allocate(1024);
    }
    public NioSockEntity(int capacity)
    {

        dataBuffer = ByteBuffer.allocate(capacity);
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

        dataBuffer.clear();

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

        dataBuffer.clear();

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
