package connect.network.udp;

import connect.network.base.joggle.INetReceive;
import util.ReflectionCall;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UdpReceive implements INetReceive {

    protected Object mReceive;
    protected String mReceiveMethodName;
    protected DatagramSocket socket = null;

    public UdpReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    @Override
    public void setReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    protected void onRead(DatagramSocket socket) throws Exception {
        try {
            int size = socket.getReceiveBufferSize();
            byte[] buffer = new byte[size];
            DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
            socket.receive(receive);
            //注意接受方法参数是DatagramPacket
            ReflectionCall.invoke(mReceive, mReceiveMethodName, new Class[]{DatagramPacket.class}, receive);
        } catch (Exception e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw new Exception(e);
            }
        }
    }
}
