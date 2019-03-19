package connect.network.tcp;

import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
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
            InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
            if (socket == null) {
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
                }
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
                //配置socket
                task.onConfigSocket(socket);
                //开始链接
                socket.connect(address, 3000);
                //保存socket
                task.setSocket(socket);
                TcpReceive tcpReceive = task.getReceive();
                tcpReceive.setStream(socket.getInputStream());
                TcpSender tcpSender = task.getSender();
                tcpSender.setStream(socket.getOutputStream());
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
    protected void onExecRead(TcpClientTask task) {
        TcpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onRead(receive.getStream());
            } catch (Throwable e) {
                removeTask(task);
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onExecWrite(TcpClientTask task) {
        TcpSender sender = task.getSender();
        if (sender != null) {
            try {
                sender.onWrite(sender.getStream());
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
