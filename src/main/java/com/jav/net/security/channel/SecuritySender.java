package com.jav.net.security.channel;

import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.base.AbsNetSender;
import com.jav.net.security.channel.joggle.ISecuritySender;

/**
 * 安全协议数据发送者
 *
 * @author yyz
 */
public class SecuritySender implements ISecuritySender {

    /**
     * 真正数据发送者
     */
    protected final AbsNetSender mCoreSender;

    /**
     * 加密组件
     */
    protected IEncryptComponent mEncryptComponent;


    public SecuritySender(AbsNetSender sender) {
        mCoreSender = sender;
    }


    /**
     * 设置加密组件
     *
     * @param component
     */
    @Override
    public void setEncryptComponent(IEncryptComponent component) {
        this.mEncryptComponent = component;
    }

    /**
     * 获取真实的发送者
     *
     * @return
     */
    public AbsNetSender getCoreSender() {
        return mCoreSender;
    }


}
