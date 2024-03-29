package connect.network.udp;

import connect.network.base.BaseNetTask;

import java.net.DatagramSocket;

public class UdpTask extends BaseNetTask {

    private DatagramSocket mSocket;

    private UdpReceiver receive = null;
    private UdpSender sender = null;

    private final boolean isServer;
    private final boolean isBroadcast;
    private LiveTime liveTime = LiveTime.LOCAL_AREA;

    protected UdpTask(boolean isServer, boolean isBroadcast, LiveTime liveTime) {
        this.isServer = isServer;
        this.isBroadcast = isBroadcast;
        if (liveTime != null) {
            this.liveTime = liveTime;
        }
    }

    //---------------------------- set ---------------------------------------

    protected void setSocket(DatagramSocket socket) {
        this.mSocket = socket;
    }

    public void setReceive(UdpReceiver receive) {
        this.receive = receive;
    }

    public void setSender(UdpSender sender) {
        this.sender = sender;
    }

    @Override
    public void setAddress(String host, int port) {
        if (isServer) {
            super.setAddress(host, port);
        } else {
            throw new IllegalStateException("## The current task is not a service type and cannot be configured !!!");
        }
    }

    //---------------------------- get ---------------------------------------

    public LiveTime getLiveTime() {
        return liveTime;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public boolean isServer() {
        return isServer;
    }

    public DatagramSocket getSocket() {
        return mSocket;
    }

    public <T extends UdpSender> T getSender() {
        return (T) sender;
    }

    public <T extends UdpReceiver> T getReceive() {
        return (T) receive;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(DatagramSocket socket) {
//        // 创建socket并加入组播地址
//        socket.joinGroup(bcAddr);
//        // 必须是false才能开启广播功能！！
//        socket.setLoopbackMode(false);
    }


    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseSocket() {
    }
}
