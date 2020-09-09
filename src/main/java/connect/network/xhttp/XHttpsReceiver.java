package connect.network.xhttp;

import connect.network.base.joggle.INetReceiver;
import connect.network.ssl.TLSHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsReceiver extends XHttpReceiver {

    protected TLSHandler tlsHandler;

    public XHttpsReceiver(TLSHandler tlsHandler, INetReceiver receive) {
        super(receive);
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.tlsHandler = tlsHandler;
    }

    @Override
    protected void init() {
    }

    @Override
    protected void onRead(SocketChannel channel) throws Exception {
        Exception exception = null;
        ByteBuffer buffer = null;
        try {
            buffer = tlsHandler.readAndUnwrap(channel, false);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            byte[] data = null;
            if (buffer != null && buffer.position() > 0) {
                buffer.flip();
                data = new byte[buffer.limit()];
                buffer.get(data, 0, data.length);
            }
            try {
                onHttpReceive(data, data != null ? data.length : -1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (buffer != null) {
                    buffer.clear();
                }
            }
            if (exception != null) {
                onReceiveException(exception);
            }
        }
    }

}
