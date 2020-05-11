package connect.network.tcp;

import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpSender implements INetSender {

    protected Queue<byte[]> cache;
    protected OutputStream stream = null;
    protected ISenderFeedback feedback;

    public TcpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.feedback = feedback;
    }

    @Override
    public void sendData(byte[] data) {
        if (data != null) {
            cache.add(data);
        }
    }

//    @Override
//    public void sendDataNow(byte[] data) {
//        if (data != null) {
//            try {
//                stream.write(data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    protected void setStream(OutputStream stream) {
        this.stream = stream;
    }

    public OutputStream getStream() {
        return stream;
    }

    protected void onWrite() throws Exception {
        while (!cache.isEmpty() && stream != null) {
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
