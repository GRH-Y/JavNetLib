package com.jav.net.security.channel.joggle;

import com.jav.net.security.channel.base.ChannelEncryption;

/**
 * 切换加密方式回调
 *
 * @author yyz
 */
public interface IInitRespondResultCallBack {

    /**
     * 切换
     *
     * @param channelEncryption
     */
    void onInitRespondResult(boolean intercept , ChannelEncryption encryption);
}
