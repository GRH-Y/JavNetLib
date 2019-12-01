package connect.network.tcp;


import connect.network.base.joggle.INetReceive;
import util.IoEnvoy;
import util.ThreadAnnotation;

import java.io.InputStream;

public class TcpReceive implements INetReceive {

    protected Object mReceive;
    protected String mReceiveMethodName;
    protected InputStream stream = null;

    public TcpReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    protected void setStream(InputStream stream) {
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public void setReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    protected void onRead(InputStream stream) throws Exception {
        byte[] data = IoEnvoy.tryRead(stream);
        if (data != null) {
            ThreadAnnotation.disposeMessage(mReceiveMethodName, mReceive, new Class[]{byte[].class}, data);
        }
    }
}
