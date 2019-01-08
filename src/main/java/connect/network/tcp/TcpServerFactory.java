package connect.network.tcp;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TcpServerFactory extends AbstractTcpFactory<TcpServerTask> {
    private static TcpServerFactory mFactory;

    private TcpServerFactory() {
    }


    public static synchronized TcpServerFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpServerFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpServerFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    @Override
    protected boolean onConnectTask(TcpServerTask task) {
        boolean isOpenServer;
        ServerSocket serverSocket = task.getServerSocket();
        try {
            if (serverSocket == null && task.getServerHost() != null && task.getServerPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(task.getServerHost(), task.getServerPort());
                if (task.getServerPort() == 443) {
                    ServerSocketFactory sslServerSocketFactory = mSslFactory.getSSLServerSocketFactory();
                    SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
                    sslServerSocket.bind(address);
                    sslServerSocket.setUseClientMode(false);
                    serverSocket = sslServerSocket;
                } else {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(address, task.getConnectNum());
                }
            }
            if (serverSocket != null) {
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(500);
                serverSocket.setPerformancePreferences(0, 1, 2);
                task.onConfigServer(serverSocket);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            isOpenServer = serverSocket.isBound();
            task.onBindServer(isOpenServer);
        }
        return isOpenServer;
    }

    @Override
    protected void onExecRead(TcpServerTask task) {
        ServerSocket serverSocket = task.getServerSocket();
        try {
            Socket socket = serverSocket.accept();
            task.onAcceptServer(socket);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                task.onAcceptTimeoutServer();
            } else {
                e.printStackTrace();
                removeTask(task);
            }
        }
    }


    @Override
    protected void onDisconnectTask(TcpServerTask task) {
        task.onCloseServer();
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
