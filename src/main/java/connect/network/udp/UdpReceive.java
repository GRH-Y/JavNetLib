package connect.network.udp;

import connect.network.base.joggle.INetReceiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UdpReceive {

    protected INetReceiver mReceive;
    protected DatagramSocket socket = null;

    public UdpReceive(INetReceiver receive) {
        this.mReceive = receive;
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    protected void onRead() throws Exception {
        DatagramPacket receive = null;
        Exception exception = null;
        try {
            int size = socket.getReceiveBufferSize();
            byte[] buffer = new byte[size];
            receive = new DatagramPacket(buffer, buffer.length);
            socket.receive(receive);
        } catch (Exception e) {
            exception = e;
        } finally {
            notifyReceiver(receive, exception);
        }
    }

    protected void notifyReceiver(Object data, Exception exception) throws Exception {
        if (mReceive != null) {
            try {
                mReceive.onReceive(data, exception);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            if (exception != null) {
                if (!(exception instanceof SocketTimeoutException)) {
                    throw exception;
                }
            }
        }
    }
}
