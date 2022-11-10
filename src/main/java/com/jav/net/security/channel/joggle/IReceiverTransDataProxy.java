package com.jav.net.security.channel.joggle;

/**
 * 转发数据监听器
 *
 * @author yyz
 */
public interface IReceiverTransDataProxy {

    /**
     * 创建真实目标链接链接
     *
     * @param requestId 请求id
     * @param realHost  真实目标地址
     * @param port      真实目端口
     */
    void onCreateConnect(String requestId, String realHost, int port);

    /**
     * 创建远程通道回调
     *
     * @param requestId 请求id
     * @param status    状态,0 == 创建成功
     */
    void onCreateConnectStatus(String requestId, byte status);

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param pctCount  作用于udp 传输数据 记录数据包的顺序
     * @param data      转发的数据
     */
    void onTransData(String requestId, byte pctCount, byte[] data);
}
