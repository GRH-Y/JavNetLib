package connect.network.udp;

import connect.network.base.BaseNetSender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpSender extends BaseNetSender {

    protected Queue<Object> cache;
    protected DatagramSocket socket = null;

    public UdpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(Object objData) {
        if (objData == null) {
            return;
        }
        cache.add(objData);
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    protected int onHandleSendData(Object objData) throws Throwable {
        if (objData instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) objData;
            socket.send(packet);
        }
        return SEND_COMPLETE;
    }

    protected void onSendNetData() throws Throwable {
        Throwable exception = null;
        while (!cache.isEmpty()) {
            Object objData = cache.remove();
            try {
                onHandleSendData(objData);
            } catch (Throwable e) {
                if (!(e instanceof SocketTimeoutException)) {
                    exception = e;
                }
            }
            if (feedback != null) {
                feedback.onSenderFeedBack(this, objData, exception);
            }
            if (exception != null) {
                throw exception;
            }
        }
    }

}
