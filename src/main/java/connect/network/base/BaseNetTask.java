package connect.network.base;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseNetTask {

    private final int tag;

    private volatile AtomicBoolean socketCloseState;

    public BaseNetTask() {
        tag = hashCode();
        socketCloseState = new AtomicBoolean(false);
    }

    protected AtomicBoolean getSocketCloseState() {
        return socketCloseState;
    }

    /**
     * 是否正在关闭
     * @return
     */
    public boolean isCloseing() {
        return socketCloseState.get();
    }

    public int getTag() {
        return tag;
    }
}
