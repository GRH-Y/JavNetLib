package connect.network.xhttp;

import connect.network.nio.NioSender;
import connect.network.ssl.TLSHandler;
import util.DirectBufferCleaner;

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
    protected void sendDataImp(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return;
        }
        ByteBuffer appDataBuffer = ByteBuffer.allocateDirect(data.length);
        appDataBuffer.put(data);
        appDataBuffer.flip();
        try {
            tlsHandler.wrapAndWrite(channel, appDataBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            DirectBufferCleaner.clean(appDataBuffer);
        }
    }
}
