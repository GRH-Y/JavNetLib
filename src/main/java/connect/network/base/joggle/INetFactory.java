package connect.network.base.joggle;


import connect.network.base.BaseNetTask;

public interface INetFactory<T extends BaseNetTask> {

    /**
     * 添加nio任务
     *
     * @param task
     */
    boolean addTask(T task);

    /**
     * 移除nio任务
     *
     * @param task
     */
    void removeTask(T task);

    /**
     * 是否已启动
     *
     * @return true 为已启动
     */
    boolean isOpen();

    /**
     * 打开
     */
    void open();

    /**
     * 关闭
     */
    void close();

}
