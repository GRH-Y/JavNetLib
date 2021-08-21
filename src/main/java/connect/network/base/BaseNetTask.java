package connect.network.base;

import util.StringEnvoy;

public class BaseNetTask {

    protected String mHost = null;

    protected int mPort = -1;

    private volatile NetTaskStatus mTaskStatus;

    public BaseNetTask() {
        mTaskStatus = NetTaskStatus.NONE;
    }


    protected void changeTaskStatus(NetTaskStatus status) {
        synchronized (BaseNetTask.class) {
            this.mTaskStatus = status;
            onTaskState(status);
        }
    }

    /**
     * 任务状态变换回调
     *
     * @param status
     */
    protected void onTaskState(NetTaskStatus status) {
    }

    /**
     * 是否正在关闭
     *
     * @return
     */
    public NetTaskStatus getTaskStatus() {
        synchronized (BaseNetTask.class) {
            return mTaskStatus;
        }
    }

    public void setAddress(String host, int port) {
        if (StringEnvoy.isEmpty(host) || port < 0) {
            throw new IllegalStateException("host or port is invalid !!! ");
        }
        this.mHost = host;
        this.mPort = port;
    }


    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    /**
     * 当前状态链接彻底关闭，可以做资源回收工作
     */
    protected void onRecovery() {
        mHost = null;
        mPort = -1;
    }

}
