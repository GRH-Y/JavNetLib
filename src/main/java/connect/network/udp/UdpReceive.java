package connect.network.udp;

import connect.network.base.joggle.INetReceiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UdpReceive {

    protected INetReceiver<DatagramPacket> mReceive;
    protected DatagramSocket socket = null;

    public UdpReceive(INetReceiver<DatagramPacket> receive) {
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
            if (!(exception instanceof SocketTimeoutException)) {
                throw exception;
            }
        } finally {
            notifyReceiver(receive, exception);
        }
    }

    protected void notifyReceiver(DatagramPacket packet, Exception exception) {
        if (mReceive != null) {
            mReceive.onReceiveFullData(packet, exception);
        }
    }
}
