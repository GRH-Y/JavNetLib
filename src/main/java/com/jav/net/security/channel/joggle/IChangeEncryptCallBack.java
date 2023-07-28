package com.jav.net.security.channel.joggle;

/**
 * 切换加密方式回调
 *
 * @author yyz
 */
public interface IChangeEncryptCallBack {

    /**
     * 切换
     *
     * @param channelEncryption
     */
    void onChange(ChannelEncryption channelEncryption);
}
