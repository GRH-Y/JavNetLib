package connect.network.tcp;

import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientFactory extends AbstractTcpFactory<TcpClientTask> {

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
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    @Override
    protected boolean onConnectTask(TcpClientTask task) {
        boolean isConnect;
        Socket socket = task.getSocket();
        try {
            if (socket == null && task.getHost() != null && task.getPort() > 0) {
                InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
                if (task.getPort() == 443) {
                    SSLSocketFactory sslSocketFactory = mSslFactory.getSSLSocketFactory();
                    SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket();
                    sslSocketImpl.setSoTimeout(1000);
                    sslSocketImpl.connect(address);
                    sslSocketImpl.setUseClientMode(true);
                    sslSocketImpl.startHandshake();
                    socket = sslSocketImpl;
                } else {
                    socket = new Socket();
                    socket.connect(address, 3000);
                }
                task.setSocket(socket);
            }
            if (socket != null) {
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
                task.setInputStream(socket.getInputStream());
                task.setOutputStream(socket.getOutputStream());
                task.onConfigSocket(socket);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            isConnect = socket.isConnected();
            try {
                task.onConnectSocket(isConnect);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return isConnect;
    }

    @Override
    protected void onExecTask(TcpClientTask task) {
        TcpSender sender = task.getSender();
        TcpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                InputStream stream = task.getInputStream();
                receive.onRead(stream);
            } catch (Throwable e) {
                removeTask(task);
                e.printStackTrace();
            }
        }
        if (sender != null) {
            try {
                OutputStream stream = task.getOutputStream();
                sender.onWrite(stream);
            } catch (Throwable e) {
                removeTask(task);
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDisconnectTask(TcpClientTask task) {
        try {
            task.onCloseSocket();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
