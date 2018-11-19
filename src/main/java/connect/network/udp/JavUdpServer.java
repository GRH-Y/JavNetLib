package connect.network.udp;


import util.NetUtils;

/**
 * udp服务端（适用于java平台）
 * Created by dell on 8/22/2017.
 *
 * @author yyz
 */
public class JavUdpServer extends JavUdpConnect {

    public JavUdpServer(String netType, int port) {
        super(NetUtils.getLocalIp(netType), port, true);
    }

    public JavUdpServer(int port) {
        super(NetUtils.getLocalIp(null), port, true);
    }
}
