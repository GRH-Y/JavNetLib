package com.currency.net.nio;

import com.currency.net.udp.LiveTime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class NioUdpTask extends BaseNioSelectionTask {

    private DatagramChannel mChannel;

    private NioUdpReceiver mReceive;
    private NioUdpSender mSender;

    private boolean mIsServer = false;
    private boolean mIsBroadcast = false;
    private LiveTime mLiveTime = LiveTime.LOCAL_AREA;

    public void setLiveTime(LiveTime liveTime) {
        this.mLiveTime = liveTime;
    }

    public void setIsBroadcast(boolean isBroadcast) {
        this.mIsBroadcast = isBroadcast;
    }

    public boolean isBroadcast() {
        return mIsBroadcast;
    }

    public boolean isServer() {
        return mIsServer;
    }

    public LiveTime getLiveTime() {
        return mLiveTime;
    }

    public DatagramChannel getChannel() {
        return mChannel;
    }

    protected void setChannel(DatagramChannel channel) {
        this.mChannel = channel;
    }


    public void bindPort(int port) {
        if (port < 0) {
            throw new IllegalStateException("bind port is invalid !!! ");
        }
        this.mPort = port;
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

    public void setReceive(NioUdpReceiver receive) {
        this.mReceive = receive;
    }


    public NioUdpReceiver getReceiver() {
        return mReceive;
    }

    public NioUdpSender getSender() {
        return mSender;
    }

    /**
     * 配置SocketChannel
     *
     * @param channel
     */
    protected void onConfigChannel(DatagramChannel channel) {
        //监听者
//        NetworkInterface interf = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.1.181"));
//        InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("224.0.0.1"), 2000);
//        //设置组播地址
//        channel.bind(group);
//        channel.configureBlocking(true);
//        MulticastChannel multicast = channel;
//        multicast.join(group.getAddress(), interf);

        try {
            channel.connect(new InetSocketAddress(getHost(), getPort()));
//            channel.bind(new InetSocketAddress(getPort()));
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * DatagramChannel 就绪状态
     */
    protected void onReadyChannel() {
    }

    /**
     * 准备断开链接回调
     */
    protected void onCloseClientChannel() {
    }

    /**
     * 断开链接后回调
     */
    @Override
    protected void onRecovery() {
        super.onRecovery();
        mChannel = null;
    }
}
