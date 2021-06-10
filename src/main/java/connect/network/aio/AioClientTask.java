package connect.network.aio;

import connect.network.base.AbsNetFactory;
import connect.network.base.BaseTLSTask;
import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;

public class AioClientTask extends BaseTLSTask {

    private AsynchronousSocketChannel aSocketChannel;

    private AbsNetFactory factory;

    private AioSender sender;

    private AioReceiver receiver;

    public AioClientTask() {
    }

    public AioClientTask(AsynchronousSocketChannel socketChannel) {
        this.aSocketChannel = socketChannel;
    }


    //-------------------------------------------------------------------------------


    protected void setSocketChannel(AsynchronousSocketChannel aSocketChannel) {
        this.aSocketChannel = aSocketChannel;
    }

    protected void setFactory(AbsNetFactory factory) {
        this.factory = factory;
    }

    public void setSender(AioSender sender) {
        this.sender = sender;
    }

    public void setReceiver(AioReceiver receiver) {
        this.receiver = receiver;
    }

    //------------------------------------------------------------------------------------------------------

    protected AsynchronousSocketChannel getChannel() {
        return aSocketChannel;
    }

    protected AbsNetFactory getFactory() {
        return factory;
    }

    public AioSender getSender() {
        return sender;
    }

    public AioReceiver getReceiver() {
        return receiver;
    }

    public AsynchronousChannelGroup getChannelGroup() {
        return null;
    }

    @Override
    protected TLSHandler getTlsHandler() {
        return super.getTlsHandler();
    }

    //---------------------------- on ---------------------------------------

    /**
     * 配置AsynchronousSocketChannel，在连接动作之前
     *
     * @param channel
     */
    protected void onConfigClientChannel(AsynchronousSocketChannel channel) {
    }


    @Override
    protected void onHandshake(SSLEngine sslEngine, NetworkChannel channel) throws Throwable {
        super.onHandshake(sslEngine, channel);
    }

    /**
     * channel连接状态回调
     */
    protected void onConnectCompleteChannel() {
    }

    /**
     * 连接失败回调
     */
    protected void onConnectError(Throwable exc) {

    }

    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseClientChannel() {
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        this.aSocketChannel = null;
        this.factory = null;
    }
}
