package connect.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientFactory extends AbstractFactory<TcpClientTask> {

    private static TcpClientFactory mFactory;

    private TcpClientFactory() {
    }


    public synchronized static TcpClientFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpClientFactory();
                }
            }
        }
        return mFactory;
    }


    public static void destroy() {
        mFactory = null;
    }

    @Override
    protected boolean onConnectTask(TcpClientTask task) {
        boolean isConnect = false;
        Socket socket = task.getSocket();
        try {
            if (socket == null && task.getHost() != null && task.getPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
                socket = new Socket();
                socket.connect(address, 3000);
                task.setSocket(socket);
            }
            if (socket == null) {
                task.onConnectSocket(isConnect);
                return isConnect;
            }
            socket.setSoTimeout(1000);
            socket.setKeepAlive(true);
            //复用端口
            socket.setReuseAddress(true);
            //关闭接收紧急数据
            socket.setOOBInline(false);
            //关闭Nagle算法
            socket.setTcpNoDelay(true);
            //执行Socket的close方法，该方法也会立即返回
            socket.setSoLinger(true, 0);
            task.onConfigSocket(socket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isConnect = socket.isConnected();
            task.onConnectSocket(isConnect);
        }
        return isConnect;
    }

    @Override
    protected void onExecTask(TcpClientTask task) {
        Socket socket = task.getSocket();
        TcpSender sender = task.getSender();
        if (sender != null) {
            try {
                OutputStream stream = socket.getOutputStream();
                sender.onWrite(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TcpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                InputStream stream = socket.getInputStream();
                receive.onRead(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDisconnectTask(TcpClientTask task) {
        task.onCloseSocket();
        Socket socket = task.getSocket();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
