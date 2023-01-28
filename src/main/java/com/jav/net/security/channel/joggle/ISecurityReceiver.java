package com.jav.net.security.channel.joggle;


import com.jav.common.cryption.joggle.IDecryptComponent;
import com.jav.net.security.channel.SecurityProtocolParser;

/**
 * 安全协议的接收者
 *
 * @author yyz
 */
public interface ISecurityReceiver {

    /**
     * 设置协议解析器
     *
     * @param parser 协议解析器对象
     */
    void setProtocolParser(SecurityProtocolParser parser);


    /**
     * 设置解密组件
     *
     * @param decryptComponent 解密组件对象
     */
    void setDecryptComponent(IDecryptComponent decryptComponent);
}
