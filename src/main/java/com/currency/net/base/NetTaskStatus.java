package com.currency.net.base;


public class NetTaskStatus {

    /**
     * 任务闲置
     */
    public static final NetTaskStatus NONE = new NetTaskStatus(0);

    /**
     * 准备结束
     */
    public static final NetTaskStatus READY_END = new NetTaskStatus(1);

    /**
     * 任务结束
     */
    public static final NetTaskStatus FINISH = new NetTaskStatus(2);

    /**
     * 任务已加载，等待执行
     */
    public static final NetTaskStatus LOAD = new NetTaskStatus(4);

    /**
     * 任务正在执行
     */
    public static final NetTaskStatus RUN = new NetTaskStatus(8);

    /**
     * 空转状态
     */
    public static final NetTaskStatus IDLING = new NetTaskStatus(16);

    /**
     * 进行读操作
     */
    public static final NetTaskStatus READ = new NetTaskStatus(32);

    /**
     * 进行写操作
     */
    public static final NetTaskStatus WRITE = new NetTaskStatus(64);

    /**
     * 分发状态
     */
    public static final NetTaskStatus ASSIGN = new NetTaskStatus(128);


    private int status;

    public NetTaskStatus(int status) {
        this.status = status;
    }

    public int getCode() {
        return status;
    }

}
