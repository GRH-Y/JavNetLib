package com.jav.net.component.joggle;


/**
 * 缓存部件，用于receiver使用不用的缓存
 */
public interface IBufferComponent {

    /**
     * 获取buffer
     *
     * @return
     */
    <T> T useBuffer();

    /**
     * 回收buffer
     *
     * @param buffer
     */
    <T> void reuseBuffer(T buffer);

}
