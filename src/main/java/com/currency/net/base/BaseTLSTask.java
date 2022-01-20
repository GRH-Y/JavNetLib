package com.currency.net.base;

import com.currency.net.base.joggle.ISSLFactory;

public class BaseTLSTask extends BaseNetTask {

    protected boolean mIsTLS = false;

    public void setTLS(boolean TLS) {
        mIsTLS = TLS;
    }

    public boolean isTLS() {
        return mIsTLS;
    }

    /**
     * TLS 握手回调（只有是TLS通讯才会回调）
     *
     * @param sslFactory
     * @throws Exception
     */
    protected void onCreateSSLContext(ISSLFactory sslFactory) {
    }

}
