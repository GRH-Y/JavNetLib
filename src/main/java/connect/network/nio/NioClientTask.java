package connect.network.nio;


import connect.network.base.BaseNioNetTask;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNioNetTask {

    private int connectTimeout = 1000;

    private String mHost = null;

    private int mPort = -1;

    private SocketChannel mChannel = null;

    private NioSender sender = null;

    private NioReceive receive = null;

    public NioClientTask() {
    }

    public NioClientTask(String host, int port) {
        setAddress(host, port);
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

    protected void setChannel(SocketChannel channel) {
        this.mChannel = channel;
    }

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }


    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    protected void setSelectionKey(SelectionKey selectionKey) {
        super.setSelectionKey(selectionKey);
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

    public <T extends NioSender> T getSender() {
        return (T) sender;
    }

    public <T extends NioReceive> T getReceive() {
        return (T) receive;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }


    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
    }

    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseSocketChannel() {
    }

    /**
     * 当前状态链接彻底关闭，可以做资源回收工作
     */
    protected void onRecovery() {
    }
}
