package com.jav.net.base;

import com.jav.net.base.joggle.ISSLComponent;

/**
 * 基本tls的task，提供sslFactory回调
 *
 * @author yyz
 */
public class BaseTlsTask extends BaseNetTask {

    protected boolean mIsTls = false;

    public void setTls(boolean TLS) {
        mIsTls = TLS;
    }

    public boolean isTls() {
        return mIsTls;
    }

    /**
     * TLS 握手回调（只有是TLS通讯才会回调）
     *
     * @param sslFactory
     */
    protected void onCreateSSLContext(ISSLComponent sslFactory) {
    }

}
