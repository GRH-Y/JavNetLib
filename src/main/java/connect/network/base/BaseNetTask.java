package connect.network.base;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseNetTask {

    private volatile AtomicBoolean isTaskNeedClose;

    public BaseNetTask() {
        isTaskNeedClose = new AtomicBoolean(false);
    }

    protected void setTaskNeedClose(boolean isClose) {
        isTaskNeedClose.set(isClose);
    }

    /**
     * 是否正在关闭
     *
     * @return
     */
    public boolean isTaskNeedClose() {
        return isTaskNeedClose.get();
    }

}
