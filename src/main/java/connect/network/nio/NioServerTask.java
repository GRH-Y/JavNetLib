package connect.network.nio;


import javax.net.ssl.SSLEngine;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask extends BaseNioNetTask {

    private ServerSocketChannel mChannel = null;
    private int mMaxConnect = 50;

    public NioServerTask() {
    }

    public NioServerTask(ServerSocketChannel channel) {
        mChannel = channel;
    }

    //---------------------------- set ---------------------------------------

    /**
     * 获取该任务要链接的服务器地址
     *
     * @param channel
     */
    public void setServerSocketChannel(ServerSocketChannel channel) {
        this.mChannel = channel;
    }

    public void setMaxConnect(int maxConnect) {
        this.mMaxConnect = mMaxConnect;
    }

    //---------------------------- get ---------------------------------------


    public ServerSocketChannel getServerSocketChannel() {
        return mChannel;
    }

    public int getMaxConnect() {
        return mMaxConnect;
    }


    //---------------------------- on ---------------------------------------

    /**
     * 配置ServerSocketChannel，在没有开启服务前回调
     * @param channel
     */
    protected void onConfigServerChannel(ServerSocketChannel channel) {

    }

    protected void onBootServerComplete(ServerSocketChannel channel) {
    }

    @Override
    protected void onHandshake(SSLEngine sslEngine, NetworkChannel channel) throws Throwable {
        super.onHandshake(sslEngine, channel);
    }

    protected void onAcceptServerChannel(SocketChannel channel) {
    }

    protected void onCloseServerChannel() {
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        mChannel = null;
    }
}
