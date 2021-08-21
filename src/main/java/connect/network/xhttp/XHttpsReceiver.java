package connect.network.xhttp;

import connect.network.nio.NioReceiver;
import connect.network.ssl.TLSHandler;
import connect.network.xhttp.utils.MultiLevelBuf;
import util.IoEnvoy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsReceiver extends NioReceiver {

    protected TLSHandler mTLSHandler;

    public XHttpsReceiver(TLSHandler tlsHandler) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.mTLSHandler = tlsHandler;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable exception = null;
        MultiLevelBuf buf = XMultiplexCacheManger.getInstance().obtainBuf();
        int ret = IoEnvoy.FAIL;
        ByteBuffer[] cacheData = null;
        try {
            do {
                cacheData = buf.getAllBuf();
                ret = mTLSHandler.readAndUnwrap(channel, false, cacheData);
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
        notifyReceiverImp(buf, exception, ret);
    }

}
