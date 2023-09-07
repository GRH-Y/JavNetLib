package com.jav.net.nio;

import com.jav.net.base.UdpPackLiveTime;

import java.nio.channels.DatagramChannel;

public class NioUdpTask extends NioSelectionTask<DatagramChannel> {

    private NioUdpReceiver mReceive;
    private NioUdpSender mSender;

    private boolean mIsServer = false;
    private boolean mIsBroadcast = false;
    private UdpPackLiveTime mLiveTime = UdpPackLiveTime.EVERYWHERE;


    public void setLiveTime(UdpPackLiveTime liveTime) {
        this.mLiveTime = liveTime;
    }

    public void enableBroadcast(boolean isBroadcast) {
        this.mIsBroadcast = isBroadcast;
    }

    public boolean isBroadcast() {
        return mIsBroadcast;
    }

    public boolean isServer() {
        return mIsServer;
    }

    public UdpPackLiveTime getLiveTime() {
        return mLiveTime;
    }

    public void bindAddress(String host, int port) {
        super.setAddress(host, port);
        mIsServer = true;
    }

    @Override
    public void setAddress(String host, int port) {
        super.setAddress(host, port);
        mIsServer = false;
    }

    public void setSender(NioUdpSender sender) {
        this.mSender = sender;
    }

    public void setReceiver(NioUdpReceiver receive) {
        this.mReceive = receive;
    }


    public <T extends NioUdpReceiver> T getReceiver() {
        return (T) mReceive;
    }

    public <T extends NioUdpSender> T getSender() {
        return (T) mSender;
    }

    /**
     * 配置SocketChannel
     *
     * @param channel udp通道
     */
    protected void onConfigChannel(DatagramChannel channel) {
        //监听者
//        NetworkInterface inter = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.1.181"));
//        InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("224.0.0.1"), 2000);
//        //设置组播地址
//        channel.bind(group);
//        channel.configureBlocking(true);
//        MulticastChannel multicast = channel;
//        multicast.join(group.getAddress(), inter);
    }

}
