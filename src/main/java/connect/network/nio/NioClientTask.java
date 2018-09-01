package connect.network.nio;


import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask {

    private String mHost;

    private int mPort;

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

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    //---------------------------- get ---------------------------------------

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    public SocketChannel getSocketChannel() {
        return mChannel;
    }

    public NioSender getSender() {
        return sender;
    }

    public NioReceive getReceive() {
        return receive;
    }

    //---------------------------- on ---------------------------------------

    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    protected void onConnectSocketChannel(boolean isConnect) {
    }


    protected void onCloseSocketChannel() {
    }
}
