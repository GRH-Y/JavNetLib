package com.jav.net.security.channel.joggle;


import com.jav.net.security.channel.SecurityProtocolParser;

/**
 * 安全协议的接收者
 *
 * @author yyz
 */
public interface ISecurityReceiver {

    /**
     * 获取协议解析者
     *
     * @return
     */
    SecurityProtocolParser getProtocolParser();
}
