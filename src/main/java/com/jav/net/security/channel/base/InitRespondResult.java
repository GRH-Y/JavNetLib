package com.jav.net.security.channel.base;

import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.security.channel.joggle.IInitRespondResultCallBack;

/**
 * 初始化结果
 */
public class InitRespondResult {

    private final EncryptionType mEncryption;

    private final IInitRespondResultCallBack mCallback;

    public InitRespondResult(EncryptionType encryption, IInitRespondResultCallBack callBack) {
        mEncryption = encryption;
        mCallback = callBack;
    }

    public void finish(boolean intercept) {
        if (mCallback != null) {
            mCallback.onInitRespondResult(intercept, mEncryption);
        }
    }

}
