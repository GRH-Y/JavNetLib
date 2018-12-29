package connect.network.udp;

import connect.network.base.joggle.ISender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpSender implements ISender {

    private Queue<UdpSendrEntity> cache;
    private DatagramPacket mPacket;

    public UdpSender() {
        cache = new ConcurrentLinkedQueue<>();
    }

    public void setSocketAddress(InetSocketAddress address) {
        if (mPacket != null) {
            mPacket.setSocketAddress(address);
        }
    }

    @Override
    public void sendData(byte[] data) {
        cache.add(new UdpSendrEntity(data, data.length));
    }

    public void sendData(byte[] data, int length) {
        cache.add(new UdpSendrEntity(data, length));
    }

    protected void onWrite(DatagramSocket socket, InetSocketAddress address) throws Exception {
        while (!cache.isEmpty()) {
            UdpSendrEntity entity = cache.remove();
            try {
                if (mPacket == null) {
                    mPacket = new DatagramPacket(entity.data, entity.length, address);
                } else {
                    mPacket.setData(entity.data);
                    mPacket.setLength(entity.length);
                }
                socket.send(mPacket);
            } catch (Exception e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw new Exception(e);
                }
            }
        }
    }

}
