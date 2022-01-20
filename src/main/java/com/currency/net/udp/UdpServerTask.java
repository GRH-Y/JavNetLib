package com.currency.net.udp;

public class UdpServerTask extends UdpTask {

    public UdpServerTask() {
        super(true, false, null);
    }

    public UdpServerTask(boolean isBroadcast, LiveTime liveTime) {
        super(true, isBroadcast, liveTime);
    }
}
