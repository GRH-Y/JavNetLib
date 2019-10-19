package connect.network.tcp;


import connect.network.base.BaseNetTask;

import java.net.Socket;

public class TcpClientTask extends BaseNetTask {

    private Socket mSocket = null;
    private String mHost = null;
    private int mPort = -1;
    private int connectTimeout = 3000;

    private TcpReceive receive = null;
    private TcpSender sender = null;

    public TcpClientTask() {
    }

    public TcpClientTask(String host, int port) {
        setAddress(host, port);
    }

    public TcpClientTask(Socket socket) {
        setSocket(socket);
    }


    //---------------------------- set ---------------------------------------

    protected void setSocket(Socket socket) {
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

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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

    public int getConnectTimeout() {
        return connectTimeout;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigSocket(Socket socket) {
    }

    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    protected void onConnectSocket(boolean isConnect) {
    }


    /**
     * 当前状态链接还没关闭，可以做最后的一次数据传输
     */
    protected void onCloseSocket() {
    }
}
