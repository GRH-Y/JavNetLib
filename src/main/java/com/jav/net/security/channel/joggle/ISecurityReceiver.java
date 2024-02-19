package com.jav.net.security.channel.joggle;


import com.jav.common.cryption.joggle.ICipherComponent;

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
    void setProtocolParser(ISecurityProtocolParser parser);


    /**
     * 设置解密组件
     *
     * @param decryptComponent 解密组件对象
     */
    void setDecryptComponent(ICipherComponent decryptComponent);
}
