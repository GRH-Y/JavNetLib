package connect.network.tcp;


import java.net.Socket;
import java.net.SocketException;

public class TcpClientTask {

    private Socket mSocket;
    private String mHost;
    private int mPort;

    private TcpReceive receive = null;
    private TcpSender sender = null;

    //---------------------------- set ---------------------------------------

    public void setSocket(Socket socket) {
        this.mSocket = socket;
    }

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public void setReceive(TcpReceive receive) {
        this.receive = receive;
    }

    public void setSender(TcpSender sender) {
        this.sender = sender;
    }

    //---------------------------- get ---------------------------------------

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public TcpSender getSender() {
        return sender;
    }

    public TcpReceive getReceive() {
        return receive;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(Socket socket) throws SocketException {

    }

    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    protected void onConnectSocket(boolean isConnect) {
    }


    protected void onCloseSocket() {
    }
}