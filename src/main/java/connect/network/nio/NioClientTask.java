package connect.network.nio;


import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.channels.NetworkChannel;
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
        setChannel(channel);
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

    protected SocketChannel getChannel() {
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
     * 配置SocketChannel
     *
     * @param channel
     */
    protected void onConfigChannel(SocketChannel channel) {

    }

    /**
     * SocketChannel 连接成功回调
     */
    protected void onConnectCompleteChannel(SocketChannel channel) {
    }

    /**
     * 连接失败回调
     */
    protected void onConnectError(Throwable throwable) {

    }

    @Override
    protected void onHandshake(SSLEngine sslEngine, NetworkChannel channel) throws Throwable {
        super.onHandshake(sslEngine, channel);
    }

    /**
     * 准备断开链接回调
     */
    protected void onCloseClientChannel() {
    }

    /**
     * 断开链接后回调
     */
    @Override
    protected void onRecovery() {
        super.onRecovery();
        mChannel = null;
        if (sender != null) {
            sender.clear();
        }
    }
}
