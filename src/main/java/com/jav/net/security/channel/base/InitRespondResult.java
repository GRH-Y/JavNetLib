package com.jav.net.security.channel.base;

import com.jav.net.security.channel.joggle.IInitRespondResultCallBack;

/**
 * 初始化结果
 */
public class InitRespondResult {

    private final ChannelEncryption mEncryption;

    private final IInitRespondResultCallBack mCallback;

    public InitRespondResult(ChannelEncryption encryption, IInitRespondResultCallBack callBack) {
        mEncryption = encryption;
        mCallback = callBack;
    }

    public void finish(boolean intercept) {
        if (mCallback != null) {
            mCallback.onInitRespondResult(intercept, mEncryption);
        }
    }

}
