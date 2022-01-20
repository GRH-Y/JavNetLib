package com.currency.net.base;

import util.StringEnvoy;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseNetTask {

    protected String mHost = null;
    protected int mPort = -1;

    private volatile AtomicInteger mTaskStatus;

    private final int ACTION_SET = 0;
    private final int ACTION_ADD = 1;
    private final int ACTION_DEL = 2;

    public BaseNetTask() {
        mTaskStatus = new AtomicInteger(NetTaskStatus.NONE.getCode());
    }

    protected void setTaskStatus(NetTaskStatus... status) {
        changeTaskStatus(ACTION_SET, status);
    }

    protected void addTaskStatus(NetTaskStatus... status) {
        changeTaskStatus(ACTION_ADD, status);
    }

    protected void waitAndSetTaskStatus(NetTaskStatus waitStatus, NetTaskStatus setStatus) {
        while (!mTaskStatus.compareAndSet(waitStatus.getCode(), setStatus.getCode())) {
            try {
                synchronized (mTaskStatus) {
                    mTaskStatus.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void delTaskStatus(NetTaskStatus... status) {
        changeTaskStatus(ACTION_DEL, status);
    }

    private void changeTaskStatus(int action, NetTaskStatus... status) {
        int statusCode = 0;
        switch (action) {
            case ACTION_DEL:
            case ACTION_ADD:
                statusCode = mTaskStatus.get();
            case ACTION_SET:
                for (NetTaskStatus tmp : status) {
                    if (ACTION_DEL == action) {
                        statusCode ^= tmp.getCode();
                    } else {
                        statusCode |= tmp.getCode();
                    }
                }
                break;
        }
        mTaskStatus.set(statusCode);
        notifyNetTaskStatusChange();
        onTaskState(statusCode);
    }

    private void notifyNetTaskStatusChange() {
        synchronized (mTaskStatus) {
            mTaskStatus.notifyAll();
        }
    }


    /**
     * 是否正在关闭
     *
     * @return
     */
    public int getTaskStatus() {
        return mTaskStatus.get();
    }

    public boolean isHasStatus(NetTaskStatus checkStatus) {
        return (mTaskStatus.get() & checkStatus.getCode()) == checkStatus.getCode();
    }

    /**
     * 任务状态变换回调
     *
     * @param status
     */
    protected void onTaskState(int status) {
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
