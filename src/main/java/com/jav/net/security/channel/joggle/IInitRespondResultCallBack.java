package com.jav.net.security.channel.joggle;

import com.jav.common.cryption.joggle.EncryptionType;

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
    void onInitRespondResult(boolean intercept , EncryptionType encryption);
}
