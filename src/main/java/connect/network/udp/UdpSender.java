package connect.network.udp;

import connect.network.base.joggle.ISender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpSender implements ISender {

    protected Queue<UdpSenderEntity> cache;
    protected DatagramPacket mPacket;

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
        if (data != null) {
            cache.add(new UdpSenderEntity(data, data.length));
        }
    }

    public void sendData(byte[] data, int length) {
        if (data != null && length > 0) {
            cache.add(new UdpSenderEntity(data, length));
        }
    }



    protected void onWrite(DatagramSocket socket, String host, int port) throws Exception {
        while (!cache.isEmpty()) {
            UdpSenderEntity entity = cache.remove();
            try {
                if (mPacket == null) {
                    mPacket = new DatagramPacket(entity.data, entity.length, new InetSocketAddress(host, port));
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
