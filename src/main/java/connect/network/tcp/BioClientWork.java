package connect.network.tcp;


import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

public class BioClientWork<T extends TcpClientTask> extends BioNetWork<T> {

    private TcpClientFactory mFactory;

    protected BioClientWork(TcpClientFactory factory) {
        this.mFactory = factory;
    }

    @Override
    protected void onExecuteTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onExecRead(task);
            //执行写任务
            onExecWrite(task);
        }
    }

    @Override
    public void onConnectTask(T task) {
        boolean isConnect = false;
        Socket socket = task.getSocket();
        try {
            if (socket == null) {
                if (task.getPort() == 443 && mFactory.getSslFactory() != null) {
                    SSLSocketFactory sslSocketFactory = mFactory.getSslFactory().getSSLSocketFactory();
                    SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket(task.getHost(), task.getPort());
                    sslSocketImpl.setUseClientMode(true);
                    sslSocketImpl.startHandshake();
                    socket = sslSocketImpl;
                } else {
                    socket = new Socket(task.getHost(), task.getPort());
                }
            }
            if (socket != null) {
                socket.setSoTimeout(task.getConnectTimeout());
                //复用端口
                socket.setReuseAddress(true);
                if (!(socket instanceof SSLSocket)) {
                    //关闭接收紧急数据
                    socket.setOOBInline(false);
                }
                //关闭Nagle算法
                socket.setTcpNoDelay(true);
                //执行Socket的close方法，该方法也会立即返回
                socket.setSoLinger(true, 0);
                //配置socket
                task.onConfigSocket(socket);
                isConnect = socket.isConnected();
            }
        } catch (Throwable e) {
            isConnect = false;
            mFactory.removeTask(task);
            e.printStackTrace();
        }
        if (isConnect) {
            //保存socket
            task.setSocket(socket);
            TcpReceive tcpReceive = task.getReceive();
            if (tcpReceive != null) {
                try {
                    tcpReceive.setStream(socket.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            TcpSender tcpSender = task.getSender();
            if (tcpSender != null) {
                try {
                    tcpSender.setStream(socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            task.onConnectSocket(isConnect);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void onExecRead(T task) {
        TcpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onRead(receive.getStream());
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }

    protected void onExecWrite(T task) {
        TcpSender sender = task.getSender();
        if (sender != null) {
            try {
                sender.onWrite(sender.getStream());
            } catch (Throwable e) {
                mFactory.removeTask(task);
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDisconnectTask(T task) {
        try {
            task.onCloseSocket();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Socket socket = task.getSocket();
            if (socket != null) {
                try {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onRecoveryTask(T task) {
        super.onRecoveryTask(task);
        task.setSocket(null);
    }
}
