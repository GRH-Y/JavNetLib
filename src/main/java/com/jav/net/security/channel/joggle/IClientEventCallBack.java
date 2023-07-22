package com.jav.net.security.channel.joggle;

/**
 * 解析协议回调（客户模式）
 *
 * @author yyz
 */
public interface IClientEventCallBack extends IChannelEventCallBack {

    /**
     * 响应服务端高负载状态,返回低负载的服务
     *
     * @param lowLoadHost 低负载的服务地址
     * @param lowLoadPort 低负载的服务端口
     */
    void onRespondServerHighLoadCallBack(String lowLoadHost, int lowLoadPort);

    /**
     * 服务响应返回channel id
     *
     * @param channelId
     */
    void onRespondChannelIdCallBack(String channelId);


    /**
     * 响应 request
     *
     * @param requestId 请求id
     * @param status    状态,0 == 创建成功
     */
    void onRespondRequestStatusCallBack(String requestId, byte status);
}
