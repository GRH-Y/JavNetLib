package com.jav.net.security.channel.joggle;

/**
 * 解析协议回调（客户模式）
 *
 * @author yyz
 */
public interface IClientEventCallBack extends ITransDataCallBack {

    /**
     * 客户端模式回调Init协议
     *
     * @param channelId
     */
    void onInitForClientCallBack(String channelId);


    /**
     * 创建远程通道回调
     *
     * @param requestId 请求id
     * @param status    状态,0 == 创建成功
     */
    void onConnectTargetStatusCallBack(String requestId, byte status);
}
