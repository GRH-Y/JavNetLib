package connect.network.xhttp;

import connect.network.nio.NioSender;
import connect.network.ssl.TLSHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsSender extends NioSender {

    protected TLSHandler tlsHandler;

    public XHttpsSender(TLSHandler tlsHandler, SocketChannel channel) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
        this.channel = channel;
    }

    @Override
    protected void sendDataImp(ByteBuffer data) throws IOException {
        tlsHandler.wrapAndWrite(channel, data);
    }

}
