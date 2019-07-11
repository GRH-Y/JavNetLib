package connect.network.nio;


import connect.network.base.BaseNetTask;

import javax.net.ssl.SSLSocket;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNetTask {

    private int connectTimeout = 1000;

    private String mHost;

    private int mPort;

    private SocketChannel mChannel;

    private SSLSocket mSSLSocket;

    private NioSender sender = null;

    private NioReceive receive = null;

    private boolean isAutoCheckCertificate = true;

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

    protected void setSSLSocket(SSLSocket sslSocket) {
        this.mSSLSocket = sslSocket;
    }

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setAutoCheckCertificate(boolean autoCheckCertificate) {
        isAutoCheckCertificate = autoCheckCertificate;
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

    public SSLSocket getSSLSSocket() {
        return mSSLSocket;
    }

    public NioSender getSender() {
        return sender;
    }

    public NioReceive getReceive() {
        return receive;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public boolean isAutoCheckCertificate() {
        return isAutoCheckCertificate;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(SocketChannel socket) {
    }

    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    protected void onConnectSocketChannel(boolean isConnect) {
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
