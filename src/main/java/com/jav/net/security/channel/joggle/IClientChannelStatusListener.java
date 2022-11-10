package com.jav.net.security.channel.joggle;


/**
 * 客户端通道状态监听器
 *
 * @author yyz
 */
public interface IClientChannelStatusListener extends ISecurityChannelStatusListener{

    /**
     * 远程创建目标链接状态回调
     *
     * @param requestId 请求id
     * @param status    状态0 == 创建成功
     */
    void onRemoteCreateConnect(String requestId, byte status);

}