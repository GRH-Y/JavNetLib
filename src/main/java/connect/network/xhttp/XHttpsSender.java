package connect.network.xhttp;

import connect.network.nio.NioSender;
import connect.network.ssl.TLSHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class XHttpsSender extends NioSender {

    protected TLSHandler tlsHandler;

    public XHttpsSender(TLSHandler tlsHandler, SelectionKey selectionKey, SocketChannel channel) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
        setChannel(selectionKey, channel);
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.tlsHandler = tlsHandler;
    }

    @Override
    protected void sendDataImp(ByteBuffer data) throws IOException {
        tlsHandler.wrapAndWrite(channel, data);
    }

}
