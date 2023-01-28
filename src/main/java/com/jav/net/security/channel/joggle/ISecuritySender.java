package com.jav.net.security.channel.joggle;

import com.jav.common.cryption.joggle.IEncryptComponent;

/**
 * 安全协议的发送者
 *
 * @author yyz
 */
public interface ISecuritySender {

    /**
     * 设置加密组件
     *
     * @param component
     */
    void setEncryptComponent(IEncryptComponent component);

}
