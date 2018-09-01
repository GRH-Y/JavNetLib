package connect.network.nio;


import connect.network.base.joggle.ISender;

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
    protected boolean onWrite(SocketChannel channel) throws IOException {
        boolean ret = true;
        while (!cache.isEmpty()) {
            ByteBuffer buffer = cache.remove();
            ret = channel.write(buffer) > 0;
        }
        return ret;
    }
}
