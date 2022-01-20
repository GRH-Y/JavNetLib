package com.currency.net.tcp;

import com.currency.net.base.FactoryContext;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class BioServerWork<T extends TcpServerTask> extends BioNetWork<T> {

    private ISSLFactory mSSLFactory;

    protected BioServerWork(FactoryContext intent) {
        super(intent);
        this.mSSLFactory = intent.getSSLFactory();
    }


    @Override
    protected void onRWDataTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onAcceptTask(task);
        }
    }

    @Override
    public void onConnectTask(T netTask) {
        boolean isOpenServer = false;
        ServerSocket serverSocket = netTask.getServerSocket();
        try {
            if (serverSocket == null && netTask.getHost() != null && netTask.getPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
                if (netTask.getPort() == 443 && mSSLFactory != null) {
                    ServerSocketFactory sslServerSocketFactory = mSSLFactory.getSSLServerSocketFactory();
                    SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
                    sslServerSocket.bind(address);
                    sslServerSocket.setUseClientMode(false);
                    serverSocket = sslServerSocket;
                } else {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(address, netTask.getMaxConnect());
                }
            }
            if (serverSocket != null) {
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(500);
                serverSocket.setPerformancePreferences(0, 1, 2);
                isOpenServer = serverSocket.isBound();
            }
        } catch (Throwable e) {
            isOpenServer = false;
            INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
            taskFactory.addUnExecTask(netTask);
            e.printStackTrace();
        }
        try {
            netTask.onConfigServer(isOpenServer, isOpenServer ? serverSocket : null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void onAcceptTask(T task) {
        ServerSocket serverSocket = task.getServerSocket();
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                task.onAcceptTimeoutServer();
            } else {
                e.printStackTrace();
                INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                taskFactory.addUnExecTask(task);
            }
        }
        if (socket != null) {
            try {
                task.onAcceptServer(socket);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseServer();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ServerSocket serverSocket = netTask.getServerSocket();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRecoveryTask(T netTask) {
        super.onRecoveryTask(netTask);
        netTask.setServerSocket(null);
    }
}
