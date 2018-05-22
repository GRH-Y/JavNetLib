package connect.network.nio.interfaces;

public interface INioSelectorFactory<T> {

    /**
     * 添加nio任务
     *
     * @param task
     */
    void addNioTask(T task);

    /**
     * 移除nio任务
     *
     * @param task
     */
    void removeNioTask(T task);

    /**
     * 打开
     */
    void open();

    /**
     * 关闭
     */
    void close();

}
