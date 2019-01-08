package connect.network.nio;


import connect.network.base.joggle.ISender;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSender implements ISender {

    protected Queue<ByteBuffer> cache;
    protected SocketChannel channel;

    public NioSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(byte[] data) {
        synchronized (NioSender.class) {
            cache.add(ByteBuffer.wrap(data));
            NioClientFactory.getFactory().registerWrite(this);
        }
    }

    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * 向输出流写数据
     *
     * @return 成功发送返回true
     */
    protected void onWrite(SocketChannel channel) throws Exception {
        while (!cache.isEmpty()) {
            ByteBuffer buffer = cache.remove();
            try {
                if (channel.write(buffer) <= 0) {
                    throw new Exception("NioSender onWrite error  !!!");
                }
            } catch (Exception e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw new Exception(e);
                }
            }
        }
        synchronized (NioSender.class) {
            if (cache.isEmpty()) {
                NioClientFactory.getFactory().unRegisterWrite(this);
            }
        }
    }
}
