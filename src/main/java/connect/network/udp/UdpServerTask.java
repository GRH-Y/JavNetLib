package connect.network.udp;

import connect.network.tcp.TcpReceive;
import connect.network.tcp.TcpSender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServerTask extends UdpTask {

    public UdpServerTask() {
        super(true, false, null);
    }

    public UdpServerTask(boolean isBroadcast, LiveTime liveTime) {
        super(true, isBroadcast, liveTime);
    }
}
