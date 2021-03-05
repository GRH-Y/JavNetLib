package connect.network.xhttp;

import connect.network.base.SocketChannelCloseException;
import connect.network.nio.NioReceiver;
import connect.network.ssl.TLSHandler;
import connect.network.xhttp.utils.MultilevelBuf;
import util.IoEnvoy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsReceiver extends NioReceiver {

    protected TLSHandler tlsHandler;

    public XHttpsReceiver(TLSHandler tlsHandler) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.tlsHandler = tlsHandler;
    }

    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable exception = null;
        MultilevelBuf buf = XMultiplexCacheManger.getInstance().obtainBuf();
        int ret = IoEnvoy.FAIL;
        ByteBuffer[] cacheData = null;
        try {
            do {
                cacheData = buf.getAllBuf();
                ret = tlsHandler.readAndUnwrap(channel, false, cacheData);
                if (ret == TLSHandler.NOT_ENOUGH_CAPACITY) {
                    //解码缓存容量不够，则需要多传byteBuffer
                    buf.setBackBuf(cacheData);
                    buf.appendBuffer();
                }
            } while (ret == TLSHandler.NOT_ENOUGH_CAPACITY);
        } catch (Throwable e) {
            exception = e;
        } finally {
            buf.setBackBuf(cacheData);
            buf.flip();
        }
        try {
            notifyReceiverImp(buf, exception);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (exception != null) {
            throw exception;
        } else if (ret < 0 && exception == null) {
            throw new SocketChannelCloseException();
        }
    }

}
