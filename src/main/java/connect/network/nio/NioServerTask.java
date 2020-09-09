package connect.network.nio;


import util.StringEnvoy;

import javax.net.ssl.SSLEngine;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask extends BaseNioNetTask {

    private String mHost = null;
    private int mPort = 0;
    private boolean isTLS = false;
    private ServerSocketChannel mChannel = null;
    private int mMaxConnect = 50;
    private int acceptTimeout = 3000;

    public NioServerTask() {
    }

    public NioServerTask(ServerSocketChannel channel) {
        mChannel = channel;
    }

    //---------------------------- set ---------------------------------------

    public void setAddress(String host, int port, boolean isTLS) {
        if (StringEnvoy.isEmpty(host) || port < 0) {
            throw new IllegalStateException("host or port is invalid !!! ");
        }
        this.mHost = host;
        this.mPort = port;
        this.isTLS = isTLS;
    }

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

    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

    //---------------------------- get ---------------------------------------

    public boolean isTLS() {
        return isTLS;
    }

    public int getServerPort() {
        return mPort;
    }

    public String getServerHost() {
        return mHost;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return mChannel;
    }

    public int getMaxConnect() {
        return mMaxConnect;
    }

    public int getAcceptTimeout() {
        return acceptTimeout;
    }

    //---------------------------- on ---------------------------------------

    protected void onBootServerComplete(ServerSocketChannel channel) throws Exception {
    }

    protected void onConfigSSLEngine(SSLEngine sslEngine) {

    }

    protected void onAcceptServerChannel(SocketChannel channel) throws Exception {
    }

    protected void onCloseServerChannel() throws Exception {
    }

    @Override
    protected void reset() {
        super.reset();
        isTLS = false;
        mChannel = null;
    }
}
