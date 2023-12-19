package com.jav.net.security.channel.joggle;


import com.jav.net.security.channel.SecurityServerChannelImage;

/**
 * 服务模式通道状态监听器
 *
 * @author yyz
 */
public interface IServerChannelStatusListener extends ISecurityChannelStatusListener<SecurityServerChannelImage> {

    /**
     * 创建真实目标链接链接
     *
     * @param requestId 请求id
     * @param realHost  真实目标地址
     * @param port      真实目标端口
     */
    void onCreateConnect(String requestId, String realHost, int port);


    /**
     * 通道接收到的数据
     *
     * @param requestId 请求id
     * @param data      中转数据
     */
    void onRequestTransData(String requestId, byte[] data);

    /**
     * 响应init协议
     *
     * @param machineId 机器id
     * @return true 拦截默认响应
     */
    boolean onRespondInitData(String machineId);


    /**
     * 发现存在同个machineId在多台设备同时使用
     */
    void onRepeatMachine(String machineId);

}