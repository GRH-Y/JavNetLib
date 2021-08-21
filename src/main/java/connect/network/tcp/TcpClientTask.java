package connect.network.tcp;


import connect.network.base.BaseNetTask;

import java.net.Socket;

public class TcpClientTask extends BaseNetTask {

    private Socket mSocket = null;
    private int mConnectTimeout = 3000;

    private TcpReceive mReceive = null;
    private TcpSender mSender = null;

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


    public void setReceive(TcpReceive receive) {
        this.mReceive = receive;
    }

    public void setSender(TcpSender sender) {
        this.mSender = sender;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.mConnectTimeout = connectTimeout;
    }

    //---------------------------- get ---------------------------------------

    public Socket getSocket() {
        return mSocket;
    }

    public <T extends TcpSender> T getSender() {
        return (T) mSender;
    }

    public <T extends TcpReceive> T getReceive() {
        return (T) mReceive;
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
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
