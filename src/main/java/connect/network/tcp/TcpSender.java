package connect.network.tcp;

import connect.network.base.BaseNetSender;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpSender extends BaseNetSender {

    protected Queue<Object> cache;
    protected OutputStream stream = null;

    public TcpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(Object objData) {
        if (objData != null) {
            cache.add(objData);
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

    protected void onSendNetData() throws Throwable {
        while (!cache.isEmpty() && stream != null) {
            Object objData = cache.remove();
            onHandleSendData(objData);
        }
    }

    @Override
    protected int onHandleSendData(Object objData) throws Throwable {
        if (objData instanceof byte[]) {
            try {
                byte[] data = (byte[]) objData;
                stream.write(data);
            } catch (Exception e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw new Exception(e);
                }
            }
        }
        return SEND_COMPLETE;
    }
}
