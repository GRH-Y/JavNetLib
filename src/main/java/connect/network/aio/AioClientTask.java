package connect.network.aio;

import connect.network.base.AbsNetFactory;
import connect.network.base.BaseTLSTask;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;

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

    protected AsynchronousSocketChannel getSocketChannel() {
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

    //---------------------------- on ---------------------------------------


    @Override
    protected void onHandshake(SSLEngine sslEngine, AsynchronousSocketChannel channel) throws Exception {
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
