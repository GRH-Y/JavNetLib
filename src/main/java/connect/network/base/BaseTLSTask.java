package connect.network.base;

import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;

public class BaseTLSTask extends BaseNetTask {

    protected boolean mIsTLS = false;

    protected TLSHandler mTLSHandler = null;

    public void setTLS(boolean TLS) {
        mIsTLS = TLS;
    }

    public boolean isTLS() {
        return mIsTLS;
    }

    protected TLSHandler getTlsHandler() {
        return mTLSHandler;
    }


    /**
     * TLS 握手回调（只有是TLS通讯才会回调）
     *
     * @param sslEngine
     * @param channel
     * @throws Exception
     */
    protected void onHandshake(SSLEngine sslEngine, NetworkChannel channel) throws Throwable {
        mTLSHandler = new TLSHandler(sslEngine);
        sslEngine.beginHandshake();
        if (channel instanceof SocketChannel) {
            mTLSHandler.doHandshake((SocketChannel) channel);
        } else if (channel instanceof AsynchronousSocketChannel) {
            mTLSHandler.doHandshake((AsynchronousSocketChannel) channel);
        }
    }



    @Override
    protected void onRecovery() {
        super.onRecovery();
        mIsTLS = false;
        mTLSHandler = null;
    }
}
