package connect.network.tcp;

import connect.network.base.joggle.ISender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpSender implements ISender {

    private Queue<byte[]> cache;

    public TcpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(byte[] data) {
        cache.add(data);
    }

    protected void onWrite(OutputStream stream) {
        while (!cache.isEmpty()) {
            byte[] data = cache.remove();
            try {
                stream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
