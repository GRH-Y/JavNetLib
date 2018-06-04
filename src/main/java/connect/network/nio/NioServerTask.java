package connect.network.nio;


import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask {

    private ServerSocketChannel channel = null;
    private InetSocketAddress address;

    //---------------------------- set ---------------------------------------

    public void setAddress(String ip, int port) {
        address = new InetSocketAddress(ip, port);
    }

    /**
     * 获取该任务要链接的服务器地址
     *
     * @param channel
     */
    public void setSocketChannel(ServerSocketChannel channel) {
        this.channel = channel;
    }
    
    //---------------------------- get ---------------------------------------

    protected ServerSocketChannel getSocketChannel() {
        return channel;
    }

    protected InetSocketAddress getSocketAddress() {
        return address;
    }

    //---------------------------- on ---------------------------------------

    protected void onAccept(SocketChannel channel) {
    }

    protected void onOpenServer(boolean isSuccess) {
    }

    protected void onCloseServer() {
    }
}
