package connect.network.tcp;

import connect.network.base.Interface.IReceive;
import task.utils.IoUtils;
import task.utils.ThreadAnnotation;

import java.io.InputStream;

public class TcpReceive implements IReceive {

    private Object mReceive;
    private String mReceiveMethodName;

    public TcpReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    @Override
    public void setReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;

    }

    protected void onRead(InputStream stream) {
        byte[] data = IoUtils.tryRead(stream);
        if (data != null) {
            ThreadAnnotation.disposeMessage(mReceiveMethodName, mReceive, data);
        }
    }
}
