package com.jav.net.security.channel.joggle;


/**
 * 通道状态监听器
 *
 * @author yyz
 */
public interface IServerChannelStatusListener extends ISecurityChannelStatusListener{

    /**
     * 创建真实目标链接链接
     *
     * @param requestId 请求id
     * @param realHost  真实目标地址
     * @param port      真实目标端口
     */
    void onCreateConnect(String requestId, String realHost, int port);

}