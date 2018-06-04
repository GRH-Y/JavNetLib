package connect.network.nio;


import connect.network.base.Interface.ISender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSender implements ISender {

    private Queue<ByteBuffer> cache;

    public NioSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(byte[] data) {
        cache.add(ByteBuffer.wrap(data));
    }

    /**
     * 向输出流写数据
     *
     * @return 成功发送返回true
     */
    protected void onWrite(SocketChannel channel) {
        while (cache.size() > 0) {
            ByteBuffer buffer = cache.remove();
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
