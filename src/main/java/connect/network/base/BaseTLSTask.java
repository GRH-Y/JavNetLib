package connect.network.base;

import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;

public class BaseTLSTask extends BaseNetTask {

    protected boolean isTLS = false;

    protected TLSHandler tlsHandler = null;

    public void setTLS(boolean TLS) {
        isTLS = TLS;
    }

    public boolean isTLS() {
        return isTLS;
    }

    protected TLSHandler getTlsHandler() {
        return tlsHandler;
    }


    /**
     * TLS 握手回调（只有是TLS通讯才会回调）
     *
     * @param sslEngine
     * @param channel
     * @throws Exception
     */
    protected void onHandshake(SSLEngine sslEngine, NetworkChannel channel) throws Throwable {
        tlsHandler = new TLSHandler(sslEngine);
        sslEngine.beginHandshake();
        if (channel instanceof SocketChannel) {
            tlsHandler.doHandshake((SocketChannel) channel);
        } else if (channel instanceof AsynchronousSocketChannel) {
            tlsHandler.doHandshake((AsynchronousSocketChannel) channel);
        }
    }



    @Override
    protected void onRecovery() {
        super.onRecovery();
        isTLS = false;
        tlsHandler = null;
    }
}
