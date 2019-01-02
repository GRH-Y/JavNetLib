package connect.network.udp;

import connect.network.base.joggle.ISender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UdpSender implements ISender {

    private Queue<UdpSenderEntity> cache;
    private DatagramPacket mPacket;

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

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
            wakeUpWait();
        }
    }

    public void sendData(byte[] data, int length) {
        if (data != null && length > 0) {
            cache.add(new UdpSenderEntity(data, length));
            wakeUpWait();
        }
    }


    protected void wakeUpWait() {
        lock.lock();
        try {
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    protected void onWrite(DatagramSocket socket, InetSocketAddress address, boolean isHasReceive) throws Exception {
        if (!isHasReceive && cache.isEmpty()) {
            lock.lock();
            try {
                condition.await();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        while (!cache.isEmpty()) {
            UdpSenderEntity entity = cache.remove();
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
