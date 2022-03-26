package com.currency.net.entity;

public enum NetTaskStatusCode {

    /**
     * 任务失效
     */
    INVALID(-1),

    /**
     * 任务结束
     */
    FINISH(1),

    /**
     * 任务还没开始
     */
    NONE(2),

    /**
     * 任务已加载，等待执行
     */
    LOAD(4),

    /**
     * 处理客户端链接
     */
    RUN(8),

    /**
     * 空转状态
     */
    IDLING(16);


    private int mStatus = 0;

    NetTaskStatusCode(int code) {
        setCode(code);
    }

    public int getCode() {
        return mStatus;
    }

    public void setCode(int newCode) {
        mStatus = newCode;
    }

    public static NetTaskStatusCode getInstance(int code) {
        switch (code) {
            case 1:
                return FINISH;
            case 2:
                return NONE;
            case 4:
                return LOAD;
            case 8:
                return RUN;
            case 16:
                return IDLING;
            case -1:
                return INVALID;
        }
        return NONE;
    }
}
