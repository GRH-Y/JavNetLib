package connect.network.nio;

import connect.network.udp.LiveTime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class NioUdpTask extends BaseNioNetTask {

    private DatagramChannel mChannel;

    private NioUdpReceiver receive;
    private NioUdpSender sender;

    private boolean isServer = false;
    private boolean isBroadcast = false;
    private LiveTime liveTime = LiveTime.LOCAL_AREA;


    public boolean isBroadcast() {
        return isBroadcast;
    }

    public boolean isServer() {
        return isServer;
    }

    public LiveTime getLiveTime() {
        return liveTime;
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
    }

    public void setSender(NioUdpSender sender) {
        this.sender = sender;
    }

    public void setReceive(NioUdpReceiver receive) {
        this.receive = receive;
    }


    public NioUdpReceiver getReceiver() {
        return receive;
    }

    public NioUdpSender getSender() {
        return sender;
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
