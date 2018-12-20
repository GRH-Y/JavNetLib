package connect.network.tcp;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerTask {
    private ServerSocket mServerSocket;
    private String mHost;
    private int mPort;

    private int mConnectNum = 50;

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

    public int getConnectNum() {
        return mConnectNum;
    }

    //---------------------------- set ---------------------------------------

    public void setAddress(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public void setServerSocket(ServerSocket mServerSocket) {
        this.mServerSocket = mServerSocket;
    }

    public void setConnectNum(int connectNum) {
        this.mConnectNum = connectNum;
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
