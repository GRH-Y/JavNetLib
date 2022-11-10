package com.jav.net.component.joggle;


/**
 * 缓存部件，用于receiver使用不用的缓存
 *
 * @author yyz
 */
public interface IBufferComponent<T> {

    /**
     * 获取buffer
     *
     * @return
     */
    T useBuffer();

    /**
     * 回收buffer
     *
     * @param buffer
     */
    void reuseBuffer(T buffer);

    /**
     * 释放资源
     */
    void release();

}
