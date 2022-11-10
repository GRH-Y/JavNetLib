package com.jav.net.component.joggle;

/**
 * 缓存组件接口
 *
 * @param <T>
 * @author yyz
 */
public interface ICacheComponent<T> {

    /**
     * 扩展接口，用于清除缓存时
     *
     * @param <T>
     */
    interface IClearPolicy<T> {
        /**
         * 清除回调
         *
         * @param data
         */
        void clear(T data);
    }

    /**
     * 添加数据到缓存队列的最后
     *
     * @param data
     * @return
     */
    boolean addLastData(T data);

    /**
     * 添加数据到缓存队列最前面
     *
     * @param data
     * @return
     */
    boolean addFirstData(T data);

    /**
     * 从缓存队列最前面取出数据
     *
     * @return
     */
    T pollFirstData();

    /**
     * 从缓存队列最后面取出数据
     *
     * @return
     */
    T pollLastData();

    /**
     * 缓存队列的大小
     *
     * @return
     */
    int size();

    /**
     * 清除缓存数据
     *
     * @param picker
     */
    void clearCache(IClearPolicy picker);
}
