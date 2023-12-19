package com.jav.net.security.channel.joggle;

/**
 * 安全协议代理发送者
 *
 * @author yyz
 */
public interface ISecurityProxySender {

    /**
     * 响应init 请求
     *
     * @param machineId
     * @param repCode
     * @param initData
     */
    void respondToInitRequest(String machineId, byte repCode, byte[] initData);

    /**
     * 响应 connect 请求
     *
     * @param requestId
     * @param result
     */
    void respondToRequest(String requestId, byte result);


    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param address   请求的目标地址
     */
    void sendRequestData(String requestId, byte[] address);

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param data      转发的数据
     */
    void sendTransData(String requestId, byte[] data);


    /**
     * 相应 trans 请求
     *
     * @param requestId
     * @param repCode
     * @param data
     */
    void respondToTrans(String requestId, byte repCode, byte[] data);


    /**
     * 发送心跳包保活通道
     *
     * @param machineId
     */
    void sendKeepAlive(String machineId);
}
