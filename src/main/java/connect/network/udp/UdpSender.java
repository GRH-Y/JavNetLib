package connect.network.udp;

import connect.network.base.BaseNetSender;

import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpSender extends BaseNetSender {

    protected Queue<Object> cache;
    protected DatagramPacket mPacket;
    protected DatagramSocket socket = null;

    public UdpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(Object objData) {
        if (objData == null) {
            return;
        }
        if (objData instanceof UdpSenderCarrier) {
            UdpSenderCarrier data = (UdpSenderCarrier) objData;
            cache.add(data);
        } else {
            cache.add(objData);
        }
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    @Override
    protected int onHandleSendData(Object objData) throws Throwable {
        if (objData instanceof UdpSenderCarrier) {
            UdpSenderCarrier carrier = (UdpSenderCarrier) objData;
            if (mPacket == null) {
                mPacket = new DatagramPacket(carrier.data, carrier.length, new InetSocketAddress(carrier.host, carrier.port));
            } else {
                InetAddress address = mPacket.getAddress();
                if (!address.getHostAddress().equals(carrier.host)) {
                    mPacket.setAddress(InetAddress.getByName(carrier.host));
                }
                if (mPacket.getPort() != carrier.port) {
                    mPacket.setPort(carrier.port);
                }
            }
            mPacket.setData(carrier.data);
            mPacket.setLength(carrier.length);
            socket.send(mPacket);
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
