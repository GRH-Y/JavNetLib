package connect.network.xhttp;

import connect.network.base.joggle.INetReceiver;
import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsReceiver extends XHttpReceiver {

    protected TLSHandler tlsHandler;
    protected SSLEngine sslEngine;
    protected ByteBuffer receiveBuffer;
    protected ByteCacheStream result;

    public XHttpsReceiver(TLSHandler tlsHandler, INetReceiver receive) {
        super(receive);
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
        this.sslEngine = tlsHandler.getSslEngine();
        this.receiveBuffer = tlsHandler.newApplicationBuffer();
        this.result = new ByteCacheStream();
    }

    @Override
    protected void onRead(SocketChannel channel) throws Exception {
        result.reset();
        receiveBuffer.clear();
        Exception exception = null;
        try {
            tlsHandler.readAndUnwrap(result, receiveBuffer, false);
        } catch (Exception e) {
            exception = e;
        } finally {
            onHttpReceive(result.getBuf(), result.size(), exception);
        }
    }

}
