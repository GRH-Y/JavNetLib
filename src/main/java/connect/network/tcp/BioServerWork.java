package connect.network.tcp;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class BioServerWork<T extends TcpServerTask> extends BioNetWork<T> {

    private TcpServerFactory mFactory;

    protected BioServerWork(TcpServerFactory factory) {
        this.mFactory = factory;
    }


    @Override
    protected void onExecuteTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onAcceptTask(task);
        }
    }

    @Override
    public void onConnectTask(T task) {
        boolean isOpenServer = false;
        ServerSocket serverSocket = task.getServerSocket();
        try {
            if (serverSocket == null && task.getHost() != null && task.getPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
                if (task.getPort() == 443 && mFactory.getSSLFactory() != null) {
                    ServerSocketFactory sslServerSocketFactory = mFactory.getSSLFactory().getSSLServerSocketFactory();
                    SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
                    sslServerSocket.bind(address);
                    sslServerSocket.setUseClientMode(false);
                    serverSocket = sslServerSocket;
                } else {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(address, task.getMaxConnect());
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
            mFactory.removeTask(task);
            e.printStackTrace();
        }
        try {
            task.onConfigServer(isOpenServer, isOpenServer ? serverSocket : null);
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
                mFactory.removeTask(task);
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
    public void onDisconnectTask(T task) {
        try {
            task.onCloseServer();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ServerSocket serverSocket = task.getServerSocket();
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
    public void onRecoveryTask(T task) {
        super.onRecoveryTask(task);
        task.setServerSocket(null);
    }
}
