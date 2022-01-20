package com.currency.net.tcp;


import com.currency.net.base.FactoryContext;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;
import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

public class BioClientWork<T extends TcpClientTask> extends BioNetWork<T> {

    private ISSLFactory mSSLFactory;

    protected BioClientWork(FactoryContext intent) {
        super(intent);
        this.mSSLFactory = intent.getSSLFactory();
    }

    @Override
    protected void onRWDataTask() {
        for (T task : mExecutorQueue) {
            //执行读任务
            onExecRead(task);
            //执行写任务
            onExecWrite(task);
        }
    }

    @Override
    public void onConnectTask(T netTask) {
        boolean isConnect = false;
        Socket socket = netTask.getSocket();
        try {
            if (socket == null) {
                if (netTask.getPort() == 443 && mSSLFactory != null) {
                    SSLSocketFactory sslSocketFactory = mSSLFactory.getSSLSocketFactory();
                    SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket(netTask.getHost(), netTask.getPort());
                    sslSocketImpl.setUseClientMode(true);
                    sslSocketImpl.startHandshake();
                    socket = sslSocketImpl;
                } else {
                    socket = new Socket(netTask.getHost(), netTask.getPort());
                }
            }
            if (socket != null) {
                socket.setSoTimeout(netTask.getConnectTimeout());
                //复用端口
//                socket.setReuseAddress(true);
                if (!(socket instanceof SSLSocket)) {
                    //关闭接收紧急数据
                    socket.setOOBInline(false);
                }
                //关闭Nagle算法
                socket.setTcpNoDelay(true);
                //执行Socket的close方法，该方法也会立即返回
                socket.setSoLinger(true, 0);
                //配置socket
                netTask.onConfigSocket(socket);
                isConnect = socket.isConnected();
            }
        } catch (Throwable e) {
            isConnect = false;
            INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
            taskFactory.addUnExecTask(netTask);
            e.printStackTrace();
        }
        if (isConnect) {
            //保存socket
            netTask.setSocket(socket);
            TcpReceive tcpReceive = netTask.getReceive();
            if (tcpReceive != null) {
                try {
                    tcpReceive.setStream(socket.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            TcpSender tcpSender = netTask.getSender();
            if (tcpSender != null) {
                try {
                    tcpSender.setStream(socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            netTask.onConnectSocket(isConnect);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void onExecRead(T task) {
        TcpReceive receive = task.getReceive();
        if (receive != null) {
            try {
                receive.onReadNetData();
            } catch (Throwable e) {
                INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                taskFactory.addUnExecTask(task);
                e.printStackTrace();
            }
        }
    }

    protected void onExecWrite(T task) {
        TcpSender sender = task.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                taskFactory.addUnExecTask(task);
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseSocket();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Socket socket = netTask.getSocket();
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
    protected void onRecoveryTask(T netTask) {
        super.onRecoveryTask(netTask);
        netTask.setSocket(null);
    }
}
