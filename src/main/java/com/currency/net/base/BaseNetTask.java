package com.currency.net.base;

import util.StringEnvoy;

public class BaseNetTask {

    protected String mHost = null;
    protected int mPort = -1;

    private final NetTaskStatus mTaskStatus;

    public BaseNetTask() {
        mTaskStatus = new NetTaskStatus(NetTaskStatusCode.NONE);
    }

    /**
     * 是否正在关闭
     *
     * @return
     */
    public NetTaskStatusCode getTaskStatus() {
        return mTaskStatus.getCode();
    }

    /**
     * 设置状态
     *
     * @param newStatus
     */
    protected void setTaskStatus(NetTaskStatusCode newStatus) {
        NetTaskStatusCode statusCode = mTaskStatus.getCode();
        if (statusCode.getCode() < NetTaskStatusCode.NONE.getCode()) {
            if (newStatus.getCode() > NetTaskStatusCode.NONE.getCode()) {
                throw new IllegalStateException("Illegal state, the current state is over and cannot be changed");
            }
        }
        mTaskStatus.setCode(newStatus);
        onTaskState(mTaskStatus);
    }

    protected boolean updateTaskStatus(NetTaskStatusCode expectStatus, NetTaskStatusCode setStatus) {
        return updateTaskStatus(expectStatus, setStatus, false);
    }

    /**
     * 符合期望的状态才设置新的状态
     *
     * @param expectStatus 期望状态
     * @param setStatus    新的状态
     * @param isWait       如果为true则在修改状态失败的时候会进入等待，直到修改状态成功才返回
     * @return 设置状态成功则返回true
     */
    protected boolean updateTaskStatus(NetTaskStatusCode expectStatus, NetTaskStatusCode setStatus, boolean isWait) {
        boolean result = mTaskStatus.updateCode(expectStatus, setStatus);
        if (isWait && !result) {
            synchronized (mTaskStatus) {
                try {
                    mTaskStatus.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            result = mTaskStatus.updateCode(expectStatus, setStatus);
        }
        if (result) {
            synchronized (mTaskStatus) {
                mTaskStatus.notify();
            }
            onTaskState(mTaskStatus);
        }
        return result;
    }

    /**
     * 任务状态变化回调
     *
     * @param status
     */
    protected void onTaskState(NetTaskStatus status) {
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
