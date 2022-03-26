package com.currency.net.base;

import com.currency.net.entity.NetTaskStatus;
import com.currency.net.entity.NetTaskStatusCode;
import util.StringEnvoy;

public class BaseNetTask {

    protected String mHost = null;
    protected int mPort = -1;

    private final NetTaskStatus mCurTaskStatus;

    public BaseNetTask() {
        mCurTaskStatus = new NetTaskStatus(NetTaskStatusCode.NONE);
    }

    /**
     * 是否正在关闭
     *
     * @return
     */
    public NetTaskStatusCode getTaskStatus() {
        return mCurTaskStatus.getCode();
    }

    /**
     * 设置状态
     *
     * @param newStatus
     */
    protected void setTaskStatus(NetTaskStatusCode newStatus) {
        mCurTaskStatus.setCode(newStatus);
        onTaskState(mCurTaskStatus);
    }

    /**
     * 更新task状态
     * @param expectStatus
     * @param setStatus
     * @return
     */
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
        boolean result = mCurTaskStatus.updateCode(expectStatus, setStatus);
        if (isWait && !result) {
            synchronized (mCurTaskStatus) {
                try {
                    mCurTaskStatus.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            result = mCurTaskStatus.updateCode(expectStatus, setStatus);
        }
        if (result) {
            synchronized (mCurTaskStatus) {
                mCurTaskStatus.notify();
            }
            onTaskState(mCurTaskStatus);
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
