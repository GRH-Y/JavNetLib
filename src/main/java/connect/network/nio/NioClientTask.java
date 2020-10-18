package connect.network.nio;


import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNioNetTask {

    private SocketChannel mChannel = null;

    private NioSender sender = null;

    private NioReceiver receive = null;

    public NioClientTask() {
    }

    public NioClientTask(SocketChannel channel, TLSHandler tlsHandler) {
        if (!channel.isOpen() || !channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is bed !!! ");
        }
        this.mChannel = channel;
        this.tlsHandler = tlsHandler;
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


    //---------------------------- get ---------------------------------------

    protected SocketChannel getSocketChannel() {
        return mChannel;
    }

    public <T extends NioSender> T getSender() {
        return (T) sender;
    }

    public <T extends NioReceiver> T getReceiver() {
        return (T) receive;
    }

    @Override
    protected TLSHandler getTlsHandler() {
        return super.getTlsHandler();
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
     * 连接失败回调
     */
    protected void onConnectError(Throwable throwable) {

    }

    @Override
    protected void onHandshake(SSLEngine sslEngine, SocketChannel channel) throws Exception {
        super.onHandshake(sslEngine, channel);
    }

    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseClientChannel() {
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        mChannel = null;
        if (sender != null) {
            sender.reset();
        }
    }
}
