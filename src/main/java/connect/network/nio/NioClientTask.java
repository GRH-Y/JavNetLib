package connect.network.nio;


import connect.network.ssl.TLSHandler;
import util.StringEnvoy;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNioNetTask {

    private String mHost = null;

    private int mPort = -1;

    private boolean isTLS = false;

    private SocketChannel mChannel = null;

    private SSLEngine sslEngine = null;

    private TLSHandler tlsHandler = null;

    private NioSender sender = null;

    private NioReceiver receive = null;

    public NioClientTask() {
    }

    public NioClientTask(SocketChannel channel, SSLEngine sslEngine) {
        if (!channel.isOpen() || !channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is bed !!! ");
        }
        this.mChannel = channel;
        this.sslEngine = sslEngine;
    }

    //---------------------------- set ---------------------------------------

    public void setSender(NioSender sender) {
        this.sender = sender;
    }

    public void setReceive(NioReceiver receive) {
        this.receive = receive;
    }

    protected void setChannel(SocketChannel channel) {
        this.mChannel = channel;
    }

    protected void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public void setAddress(String host, int port, boolean isTLS) {
        if (StringEnvoy.isEmpty(host) || port < 0) {
            throw new IllegalStateException("host or port is invalid !!! ");
        }
        this.mHost = host;
        this.mPort = port;
        this.isTLS = isTLS;
    }

    //---------------------------- get ---------------------------------------


    public boolean isTLS() {
        return isTLS;
    }

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    protected SocketChannel getSocketChannel() {
        return mChannel;
    }

    protected SSLEngine getSslEngine() {
        return sslEngine;
    }

    protected TLSHandler getTlsHandler() {
        return tlsHandler;
    }

    public <T extends NioSender> T getSender() {
        return (T) sender;
    }

    public <T extends NioReceiver> T getReceive() {
        return (T) receive;
    }


    //---------------------------- on ---------------------------------------

    /**
     * channel连接状态回调
     *
     * @param channel
     */
    protected void onConnectCompleteChannel(SocketChannel channel) throws Exception {
    }

    /**
     * TLS 握手回调（只有是TLS通讯才会回调）
     *
     * @param sslEngine
     * @param channel
     * @throws Exception
     */
    protected void onHandshake(SSLEngine sslEngine, SocketChannel channel) throws Exception {
        tlsHandler = new TLSHandler(sslEngine, channel);
        tlsHandler.doHandshake();
    }

    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseClientChannel() throws Exception {
    }


}
