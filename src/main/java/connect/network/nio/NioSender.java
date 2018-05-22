package connect.network.nio;


import connect.network.nio.interfaces.INioFactorySetting;
import connect.network.nio.interfaces.INioSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSender implements INioSender {

    private Queue<ByteBuffer> cache;
    private INioFactorySetting setting;

    public NioSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    public void setSetting(INioFactorySetting setting) {
        this.setting = setting;
    }

    public void sendData(byte[] data) {
        cache.add(ByteBuffer.wrap(data));
    }

    @Override
    public void write(SocketChannel channel) {
        while (cache.size() > 0) {
            ByteBuffer buffer = cache.remove();
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (setting != null) {
            setting.disableWriteEvent();
        }
    }
}
