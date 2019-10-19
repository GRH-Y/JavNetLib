package connect.network.udp;

import connect.network.base.BaseNetWork;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class UdpWork extends BaseNetWork<UdpTask> {

    private UdpFactory mFactory;

    protected UdpWork(UdpFactory factory) {
        this.mFactory = factory;
    }

    @Override
    public void onConnectTask(UdpTask task) {
        InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
        try {
            DatagramSocket socket;
            if (task.isServer()) {
                //多播UDP
                if (task.isBroadcast() && task.getLiveTime() != null) {
                    //单播UDP服务端
                    MulticastSocket multicastSocket = new MulticastSocket(address);
                    multicastSocket.setTimeToLive(task.getLiveTime().getTtl());
                    socket = multicastSocket;
                } else {
                    //单播UDP服务端
                    socket = new DatagramSocket(address);
                }
                //0x02 成本低, 0x04 可靠性,0x08 通过输入 ,0x10 低延迟
                socket.setTrafficClass(0x02);
            } else {
                //多播UDP
                if (task.isBroadcast() && task.getLiveTime() != null) {
                    //单播UDP客户端
                    MulticastSocket multicastSocket = new MulticastSocket();
                    multicastSocket.setTimeToLive(task.getLiveTime().getTtl());
                    socket = multicastSocket;
                } else {
                    //单播UDP客户端
                    socket = new DatagramSocket();
                }
                socket.setTrafficClass(0x10);
            }
            socket.setSoTimeout(100);
            task.onConfigSocket(socket);
            task.setSocket(socket);
            if (task.getSender() != null) {
                task.getSender().setSocket(socket);
            }
            if (task.getReceive() != null) {
                task.getReceive().setSocket(socket);
            }
        } catch (Exception e) {
            task.setSocket(null);
            mFactory.removeTask(task);
            e.printStackTrace();
        }
    }

    protected void onExecRead(UdpTask task) {
        UdpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onRead(task.getSocket());
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }

    protected void onExecWrite(UdpTask task) {
        UdpSender sender = task.getSender();
        if (sender != null) {
            try {
                sender.onWrite(task.getSocket(), task.getHost(), task.getPort());
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisconnectTask(UdpTask task) {
        try {
            task.onCloseSocket();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatagramSocket socket = task.getSocket();
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public void onRecoveryTask(UdpTask task) {

    }

    @Override
    public void onRecoveryTaskAll() {

    }
}
