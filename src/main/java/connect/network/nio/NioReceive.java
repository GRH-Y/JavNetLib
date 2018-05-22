package connect.network.nio;


import connect.network.nio.interfaces.INioReceive;
import task.utils.IoUtils;
import task.utils.ThreadAnnotation;

import java.nio.channels.SocketChannel;

public class NioReceive implements INioReceive {

    private Object target;
    private String methodName;

    public NioReceive(Object target, String methodName) {
        this.target = target;
        this.methodName = methodName;
    }

    @Override
    public void read(SocketChannel channel) {
        byte[] data = IoUtils.tryRead(channel);
        if (data != null) {
            ThreadAnnotation.disposeMessage(methodName, target, data);
        }
    }
}
