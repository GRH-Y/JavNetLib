package connect.network.base.joggle;

public interface IFactory<T> {

    /**
     * 添加nio任务
     *
     * @param task
     */
    void addTask(T task);

    /**
     * 移除nio任务
     *
     * @param task
     */
    void removeTask(T task);

    /**
     * 打开
     */
    void open();

    /**
     * 关闭
     */
    void close();


}
