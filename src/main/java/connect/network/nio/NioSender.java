package connect.network.nio;


import connect.network.base.joggle.ISender;
import log.LogDog;

import java.io.IOException;
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
            cache.add(ByteBuffer.wrap(data));
            if (channel != null) {
                onRegisterWrite();
            }
        }
    }

    protected void onRegisterWrite() {
        NioSimpleClientFactory factory = (NioSimpleClientFactory) NioClientFactory.getFactory();
        synchronized (NioSender.class) {
            factory.registerWrite(this);
        }
    }

    protected void onUnRegisterWrite() {
        NioSimpleClientFactory factory = (NioSimpleClientFactory) NioClientFactory.getFactory();
        synchronized (NioSender.class) {
            if (cache.isEmpty()) {
                factory.unRegisterWrite(this);
            }
        }
    }

    @Override
    public void sendDataNow(byte[] data) {
        if (data != null && channel != null) {
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
            if (channel.write(buffer) <= 0) {
                LogDog.e("NioSender onWrite error  !!!");
                break;
            }
        }
        if (cache.isEmpty()) {
            onUnRegisterWrite();
        }
    }
}
