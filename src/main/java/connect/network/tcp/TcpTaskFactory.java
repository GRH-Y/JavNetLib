package connect.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class TcpTaskFactory extends AbstractFactory<TcpClientTask> {


    private static class LazyHolder {
        private static final TcpTaskFactory INSTANCE = new TcpTaskFactory();
    }

    private TcpTaskFactory() {
    }


    public static TcpTaskFactory getFactory() {
        return LazyHolder.INSTANCE;
    }

    @Override
    protected boolean onConnectTask(TcpClientTask task) {
        boolean isConnect = true;
        try {
            SocketAddress address = task.getSocketAddress();
            Socket socket = new Socket();
            socket.connect(address, task.getTimeout());
            task.onConnect(isConnect);
            socket.setSoTimeout(task.getTimeout());
            socket.setKeepAlive(true);
            //复用端口
            socket.setReuseAddress(true);
            //关闭接收紧急数据
            socket.setOOBInline(false);
            //关闭Nagle算法
            socket.setTcpNoDelay(true);
            //执行Socket的close方法，该方法也会立即返回
            socket.setSoLinger(true, 0);
            task.setSocket(socket);
        } catch (Exception e) {
            isConnect = false;
            e.printStackTrace();
            task.onConnect(isConnect);
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
        task.onClose();
    }
}
