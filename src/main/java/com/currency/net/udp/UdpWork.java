package com.currency.net.udp;

import com.currency.net.base.FactoryContext;
import com.currency.net.base.joggle.INetTaskContainer;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class UdpWork<T extends UdpTask> extends BioNetWork<T> {

    protected UdpWork(FactoryContext intent) {
        super(intent);
    }

    @Override
    protected void onRWDataTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onExecRead(task);
            //执行写任务
            onExecWrite(task);
        }
    }

    @Override
    public void onConnectTask(T netTask) {
        try {
            //广播地址的范围是224.0.0.0 ~ 239.255.255.255
            DatagramSocket socket;
            if (netTask.isServer()) {
                InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
                if (netTask.isBroadcast() && netTask.getLiveTime() != null) {
                    //组播UDP服务端
                    MulticastSocket multicastSocket = new MulticastSocket(address);
                    multicastSocket.setTimeToLive(netTask.getLiveTime().getTtl());
                    socket = multicastSocket;
                } else {
                    //单播UDP服务端
                    socket = new DatagramSocket(address);
                }
                //0x02 成本低, 0x04 可靠性,0x08 通过输入 ,0x10 低延迟
                socket.setTrafficClass(0x02);
            } else {
                if (netTask.isBroadcast() && netTask.getLiveTime() != null) {
                    //组播UDP客户端
                    MulticastSocket multicastSocket = new MulticastSocket();
                    multicastSocket.setTimeToLive(netTask.getLiveTime().getTtl());
                    socket = multicastSocket;
                } else {
                    //单播UDP客户端
                    socket = new DatagramSocket();
                }
                socket.setTrafficClass(0x10);
            }
            socket.setSoTimeout(100);
            netTask.onConfigSocket(socket);
            netTask.setSocket(socket);
            if (netTask.getSender() != null) {
                netTask.getSender().setSocket(socket);
            }
            if (netTask.getReceive() != null) {
                netTask.getReceive().setSocket(socket);
            }
        } catch (Throwable e) {
            netTask.setSocket(null);
            INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
            taskFactory.addUnExecTask(netTask);
            e.printStackTrace();
        }
    }

    protected void onExecRead(T task) {
        UdpReceiver receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onReadNetData();
            } catch (Throwable e) {
                INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                taskFactory.addUnExecTask(task);
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
                INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                taskFactory.addUnExecTask(task);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseSocket();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatagramSocket socket = netTask.getSocket();
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public void onRecoveryTask(T netTask) {
        super.onRecoveryTask(netTask);
        netTask.setSocket(null);
    }

}
