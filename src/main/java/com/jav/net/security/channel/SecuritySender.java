package com.jav.net.security.channel;

import com.jav.common.cryption.joggle.ICipherComponent;
import com.jav.net.base.AbsNetSender;
import com.jav.net.security.channel.joggle.ISecuritySender;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;

/**
 * 安全协议数据发送者
 *
 * @author yyz
 */
public class SecuritySender<T> implements ISecuritySender {

    /**
     * 真正数据发送者
     */
    protected final AbsNetSender<NetworkChannel, T> mCoreSender;

    /**
     * 加密组件
     */
    protected ICipherComponent mEncryptComponent;


    public SecuritySender(AbsNetSender<NetworkChannel, T> sender) {
        mCoreSender = sender;
    }


    /**
     * 设置加密组件
     *
     * @param component
     */
    @Override
    public void setEncryptComponent(ICipherComponent component) {
        this.mEncryptComponent = component;
    }

    /**
     * 获取真实的发送者
     *
     * @return
     */
    public AbsNetSender<NetworkChannel, T> getCoreSender() {
        return mCoreSender;
    }


}
