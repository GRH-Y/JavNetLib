package connect.network.udp;


import task.utils.NetUtils;

import java.net.SocketException;

/**
 * udp服务端（适用于java平台）
 * Created by dell on 8/22/2017.
 *
 * @author yyz
 */
public class JavUdpServer extends JavUdpConnect {

    public JavUdpServer(String netType, int port) throws SocketException {
        super(NetUtils.getLocalIp(netType), port, true);
    }

    public JavUdpServer(int port) throws SocketException {
        super(NetUtils.getLocalIp(null), port, true);
    }
}
