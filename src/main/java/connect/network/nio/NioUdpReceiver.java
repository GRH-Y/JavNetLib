package connect.network.nio;

import connect.network.base.joggle.INetReceiver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpReceiver {

    private int PACKET_MAX_SIZE = 1472;

    private ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);

    private INetReceiver<ReceiverPacket> receiver;

    public static class ReceiverPacket {
        public SocketAddress fromAddress;
        public byte[] data;

        public ReceiverPacket(SocketAddress fromAddress, byte[] data) {
            this.fromAddress = fromAddress;
            this.data = data;
        }
    }

    public void setDataReceiver(INetReceiver<ReceiverPacket> receiver) {
        this.receiver = receiver;
    }

    protected void onReadNetData(DatagramChannel channel) throws Throwable {
        SocketAddress address = channel.receive(buffer);
        if (receiver != null) {
            byte[] data = null;
            if (buffer.limit() > 0) {
                buffer.flip();
                data = buffer.array();
            }
            receiver.onReceiveFullData(new ReceiverPacket(address, data), null);
        }
    }
}
