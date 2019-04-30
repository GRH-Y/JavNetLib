package connect.network.tcp;

import connect.network.base.BaseNetTask;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerTask extends BaseNetTask {
    private ServerSocket mServerSocket;
    private String mHost;
    private int mPort;

    private int mMaxConnect = 50;

    //---------------------------- get ---------------------------------------

    public ServerSocket getServerSocket() {
        return mServerSocket;
    }

    public int getServerPort() {
        return mPort;
    }

    public String getServerHost() {
        return mHost;
    }

    public int getMaxConnect() {
        return mMaxConnect;
    }
//---------------------------- set ---------------------------------------

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public void setServerSocket(ServerSocket mServerSocket) {
        this.mServerSocket = mServerSocket;
    }

    public void setMaxConnect(int maxConnect) {
        this.mMaxConnect = mMaxConnect;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigServer(ServerSocket serverSocket) {
    }

    protected void onAcceptServer(Socket socket) {
    }

    protected void onAcceptTimeoutServer() {
    }

    protected void onBindServer(boolean isSuccess) {
    }

    protected void onCloseServer() {
    }
}
