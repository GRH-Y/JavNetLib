package com.jav.net.security.channel.joggle;

/**
 * 安全协议的发送者
 *
 * @author yyz
 */
public interface ISecuritySender {

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param address   请求的目标地址
     */
    void senderRequest(String requestId, byte[] address);

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param data      转发的数据
     */
    void senderTrans(String requestId, byte[] data);
}
