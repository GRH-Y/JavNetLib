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
     * 设置ssl
     * @param sslFactory
     */
    void setSSlFactory(ISSLFactory sslFactory);

    /**
     * 打开
     */
    void open();

    /**
     * 打开高性能模式
     */
    void openHighPer();

    /**
     * 关闭
     */
    void close();


}
