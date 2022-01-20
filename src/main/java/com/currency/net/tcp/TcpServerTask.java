package com.currency.net.tcp;

import com.currency.net.base.BaseNetTask;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerTask extends BaseNetTask {
    private ServerSocket mServerSocket = null;
    private int mMaxConnect = 50;

    //---------------------------- get ---------------------------------------

    public ServerSocket getServerSocket() {
        return mServerSocket;
    }

    public int getMaxConnect() {
        return mMaxConnect;
    }
    //---------------------------- set ---------------------------------------


    public void setServerSocket(ServerSocket mServerSocket) {
        this.mServerSocket = mServerSocket;
    }

    public void setMaxConnect(int maxConnect) {
        this.mMaxConnect = mMaxConnect;
    }

    //---------------------------- on ---------------------------------------

    protected void onConfigServer(boolean isSuccess, ServerSocket serverSocket) {
    }

    protected void onAcceptServer(Socket socket) {
    }

    protected void onAcceptTimeoutServer() {
    }

    protected void onCloseServer() {
    }
}
