package connect.network.base;

public enum NetTaskStatus {

    /**
     * 任务没加载状态
     */
    NONE,

    /**
     * 任务已加载，等待执行
     */
    LOAD,

    /**
     * 任务正在执行
     */
    RUN,
}
