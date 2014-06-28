package NioComponent.nio;


import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 * Created by charlown on 14-6-25.
 * rich blood entity
 */
public class NioSockEntity {
    public int channelType;  // TCP server/client UDP server/client
    public SocketChannel channel; //TCP client UDP server/client
    public ServerSocketChannel channelTcpServer; //   channel ∩ channelTcpServer = Φ

    public int bindPort;
    public String host;
    public int port;

    public ByteBuffer dataBuffer;


    public Object handle;


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
        channel = null;
        channelTcpServer  = null;

        bindPort = -1;
        host = "";

        dataBuffer.clear();

    }

    public void decodeSocketAddress(SocketChannel channel)
    {
        InetSocketAddress address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
        host = address.getAddress().getHostAddress();
        bindPort = channel.socket().getLocalPort();
        port = address.getPort();
    }
    public void decodeSocketAddress(ServerSocketChannel channel)
    {
        bindPort = channel.socket().getLocalPort();
    }

}
