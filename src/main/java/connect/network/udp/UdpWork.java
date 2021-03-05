package connect.network.udp;

import connect.network.tcp.BioNetWork;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class UdpWork<T extends UdpTask> extends BioNetWork<T> {

    private UdpFactory mFactory;

    protected UdpWork(UdpFactory factory) {
        this.mFactory = factory;
    }

    @Override
    protected void onExecuteTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onExecRead(task);
            //执行写任务
            onExecWrite(task);
        }
    }

    @Override
    public void onConnectTask(T task) {
        try {
            //广播地址的范围是224.0.0.0 ~ 239.255.255.255
            DatagramSocket socket;
            if (task.isServer()) {
                InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
                if (task.isBroadcast() && task.getLiveTime() != null) {
                    //组播UDP服务端
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
                if (task.isBroadcast() && task.getLiveTime() != null) {
                    //组播UDP客户端
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
        } catch (Throwable e) {
            task.setSocket(null);
            mFactory.removeTask(task);
            e.printStackTrace();
        }
    }

    protected void onExecRead(T task) {
        UdpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onReadNetData();
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }

    protected void onExecWrite(T task) {
        UdpSender sender = task.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisconnectTask(T task) {
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
    public void onRecoveryTask(T task) {
        super.onRecoveryTask(task);
        task.setSocket(null);
    }

}
