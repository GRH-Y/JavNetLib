package connect.network.nio;


import connect.network.base.joggle.ISender;

import java.io.IOException;
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
        if (data != null) {
            synchronized (NioSender.class) {
                cache.add(ByteBuffer.wrap(data));
                NioSimpleClientFactory factory = (NioSimpleClientFactory) NioClientFactory.getFactory();
                factory.registerWrite(this);
            }
        }
    }

    @Override
    public void sendDataNow(byte[] data) {
        if (data != null) {
            try {
                channel.write(ByteBuffer.wrap(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        while (!cache.isEmpty() && channel != null) {
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
        if (cache.isEmpty()) {
            synchronized (NioSender.class) {
                NioSimpleClientFactory factory = (NioSimpleClientFactory) NioClientFactory.getFactory();
                factory.unRegisterWrite(this);
            }
        }
    }
}
