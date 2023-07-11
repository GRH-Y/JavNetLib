package com.jav.net.security.channel.joggle;


import com.jav.net.security.channel.SecurityClientChannelImage;

/**
 * 客户端通道状态监听器
 *
 * @author yyz
 */
public interface IClientChannelStatusListener extends ISecurityChannelStatusListener<SecurityClientChannelImage> {

    /**
     * 远程创建目标链接状态回调
     *
     * @param status 状态0 == 创建成功
     */
    void onRemoteCreateConnect(byte status);


    /**
     * 通道接收到的数据
     *
     * @param data 中转数据
     */
    void onRemoteTransData(byte[] data);

}