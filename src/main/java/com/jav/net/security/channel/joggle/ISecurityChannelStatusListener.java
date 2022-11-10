package com.jav.net.security.channel.joggle;


/**
 * 通道状态监听器
 *
 * @author yyz
 */
public interface ISecurityChannelStatusListener {

    /**
     * 本地通道已建立链接
     */
    void onChannelReady();


    /**
     * 通道接收到的数据
     *
     * @param requestId 请求id
     * @param pctCount  包的次序（用于udp协议传输）
     * @param data      中转数据
     */
    void onChannelTransData(String requestId, byte pctCount, byte[] data);


    /**
     * 通道已关闭
     */
    void onChannelInvalid();
}