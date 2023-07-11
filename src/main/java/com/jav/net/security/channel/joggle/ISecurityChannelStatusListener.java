package com.jav.net.security.channel.joggle;


import com.jav.net.security.channel.SecurityChannelImage;
import com.jav.net.security.channel.base.UnusualBehaviorType;

import java.util.Map;

/**
 * 通道状态监听器
 *
 * @author yyz
 */
public interface ISecurityChannelStatusListener<T extends SecurityChannelImage> {

    /**
     * 通道已建立链接,可以正在通信（init 协议交互完成）
     *
     * @param image 通道镜像
     */
    void onChannelReady(T image);


    /**
     * 通道已关闭
     */
    void onChannelInvalid();


    /**
     * 回调错误信息
     *
     * @param error
     * @param extData
     */
    void onChannelError(UnusualBehaviorType error, Map<String, String> extData);
}