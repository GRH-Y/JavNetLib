package connect.network.udp;

public class UdpClientTask extends UdpTask {

    public UdpClientTask() {
        super(false, false, null);
    }

    public UdpClientTask(boolean isBroadcast, LiveTime liveTime) {
        super(false, isBroadcast, liveTime);
    }

}
