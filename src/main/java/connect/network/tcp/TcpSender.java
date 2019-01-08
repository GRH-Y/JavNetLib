package connect.network.tcp;

import connect.network.base.joggle.ISender;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpSender implements ISender {

    protected Queue<byte[]> cache;

    public TcpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(byte[] data) {
        cache.add(data);
    }

    protected void onWrite(OutputStream stream) throws Exception {
        while (!cache.isEmpty()) {
            byte[] data = cache.remove();
            try {
                stream.write(data);
            } catch (Exception e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw new Exception(e);
                }
            }
        }
    }
}
