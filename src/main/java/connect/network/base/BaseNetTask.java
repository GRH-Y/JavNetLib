package connect.network.base;

import util.StringEnvoy;

public class BaseNetTask {

    protected String mHost = null;

    protected int mPort = -1;

    private volatile TaskStatus taskStatus;

    public BaseNetTask() {
        taskStatus = TaskStatus.NONE;
    }

    protected void changeTaskStatus(TaskStatus status) {
        synchronized (BaseNetTask.class) {
            this.taskStatus = status;
        }
    }

    /**
     * 是否正在关闭
     *
     * @return
     */
    public TaskStatus getTaskStatus() {
        synchronized (BaseNetTask.class) {
            return taskStatus;
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
