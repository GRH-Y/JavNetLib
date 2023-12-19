package com.jav.net.security.channel.joggle;

import com.jav.common.cryption.joggle.EncryptionType;

/**
 * 解析协议回调（服务模式）
 *
 * @author yyz
 */
public interface IServerEventCallBack extends IChannelEventCallBack {

    /**
     * 服务模式回调Init协议
     *
     * @param encryption
     * @param aesKey
     * @param machineId
     */
    void onInitForServerCallBack(EncryptionType encryption, byte[] aesKey, String machineId);

    /**
     * 创建真实目标链接链接
     *
     * @param requestId 请求id
     * @param realHost  真实目标地址
     * @param port      真实目端口
     */
    void onConnectTargetCallBack(String requestId, String realHost, int port);


    /**
     * 保活通道，相应心跳包
     *
     * @param machineId
     */
    void onKeepAliveCallBack(String machineId);
}
