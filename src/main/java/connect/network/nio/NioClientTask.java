package connect.network.nio;


import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask {

    private InetSocketAddress mAddress;

    private SocketChannel mChannel;

    private NioSender sender = null;

    private NioReceive receive = null;

    public NioClientTask() {
    }

    public NioClientTask(SocketChannel channel) {
        mChannel = channel;
    }

    //---------------------------- set ---------------------------------------

    public void setSender(NioSender sender) {
        this.sender = sender;
    }

    public void setReceive(NioReceive receive) {
        this.receive = receive;
    }

    public void setChannel(SocketChannel channel) {
        this.mChannel = channel;
    }

    public void setAddress(String ip, int port) {
        mAddress = new InetSocketAddress(ip, port);
    }

    //---------------------------- get ---------------------------------------

    protected InetSocketAddress getSocketAddress() {
        return mAddress;
    }

    protected SocketChannel getSocketChannel() {
        return mChannel;
    }

    protected NioSender getSender() {
        return sender;
    }

    protected NioReceive getReceive() {
        return receive;
    }

    //---------------------------- on ---------------------------------------
    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    protected void onConnect(boolean isConnect) {
    }


    protected void onClose() {
    }
}
