package connect.network.tcp;


import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientTask {

    private Socket socket;
    private InetSocketAddress address;
    /**
     * 超时(毫秒)
     */
    private int timeout;

    private TcpReceive receive = null;
    private TcpSender sender = null;

    //---------------------------- set ---------------------------------------

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setAddress(String ip, int port) {
        address = new InetSocketAddress(ip, port);
    }

    /**
     * 设置连接超时和读取超时
     *
     * @param timeout 超时(毫秒)
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setReceive(TcpReceive receive) {
        this.receive = receive;
    }

    public void setSender(TcpSender sender) {
        this.sender = sender;
    }

    //---------------------------- get ---------------------------------------

    protected int getTimeout() {
        return timeout;
    }

    protected Socket getSocket() {
        return socket;
    }

    protected InetSocketAddress getSocketAddress() {
        return address;
    }

    protected TcpSender getSender() {
        return sender;
    }

    protected TcpReceive getReceive() {
        return receive;
    }

    //---------------------------- on ---------------------------------------

    protected void onConnect(boolean isConnect) {

    }

    protected void onClose() {

    }
}
