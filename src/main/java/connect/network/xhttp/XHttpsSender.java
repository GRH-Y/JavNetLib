package connect.network.xhttp;

import connect.network.nio.NioSender;
import connect.network.ssl.TLSHandler;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.ByteBuffer;

public class XHttpsSender extends NioSender {

    protected TLSHandler tlsHandler;
    protected SSLEngine sslEngine;
    protected ByteBuffer sendBuffer;

    public XHttpsSender(TLSHandler tlsHandler) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
        this.sslEngine = tlsHandler.getSslEngine();
        this.channel = tlsHandler.getChannel();
        sendBuffer = tlsHandler.newPacketBuffer();
    }

    @Override
    protected void sendDataImp(byte[] data) throws IOException {
        if (data == null) {
            return;
        }
        ByteBuffer appDataBuffer = ByteBuffer.wrap(data);
        sendBuffer = tlsHandler.wrapAndWrite(appDataBuffer, sendBuffer);
    }
}
