package connect.network.udp;

import java.net.DatagramSocket;

public class UdpTask {

    private DatagramSocket mSocket;
    private String mHost;
    private int mPort;

    private UdpReceive receive = null;
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

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public void setReceive(UdpReceive receive) {
        this.receive = receive;
    }

    public void setSender(UdpSender sender) {
        this.sender = sender;
    }

    //---------------------------- get ---------------------------------------

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

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

    public UdpSender getSender() {
        return sender;
    }

    public UdpReceive getReceive() {
        return receive;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(DatagramSocket socket) {
    }


    protected void onCloseSocket() {
    }
}
