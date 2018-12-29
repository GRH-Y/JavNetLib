package connect.network.udp;

import connect.network.base.joggle.IReceive;
import util.ThreadAnnotation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UdpReceive implements IReceive {

    private Object mReceive;
    private String mReceiveMethodName;

    public UdpReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    @Override
    public void setReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    protected void onRead(DatagramSocket socket) throws Exception {
        try {
            int size = socket.getReceiveBufferSize();
            byte[] buffer = new byte[size];
            DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
            socket.receive(receive);
            //注意接受方法参数是DatagramPacket
            ThreadAnnotation.disposeMessage(mReceiveMethodName, mReceive, receive);
        } catch (Exception e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw new Exception(e);
            }
        }
    }
}
