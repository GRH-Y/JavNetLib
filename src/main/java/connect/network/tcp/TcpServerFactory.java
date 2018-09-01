package connect.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TcpServerFactory extends AbstractFactory<TcpServerTask> {
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
        mFactory = null;
    }

    @Override
    protected boolean onConnectTask(TcpServerTask task) {
        boolean isOpenServer = false;
        ServerSocket serverSocket = task.getServerSocket();
        try {
            if (serverSocket == null && task.getHost() != null && task.getPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
                serverSocket = new ServerSocket();
                serverSocket.bind(address, task.getConnectNum());
            }
            if (serverSocket == null) {
                task.onOpenServer(isOpenServer);
                return isOpenServer;
            }
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(500);
            serverSocket.setPerformancePreferences(0, 1, 2);
            task.onConfigServer(serverSocket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isOpenServer = !serverSocket.isClosed();
            task.onOpenServer(isOpenServer);
        }
        return isOpenServer;
    }

    @Override
    protected void onExecTask(TcpServerTask task) {
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
