package com.currency.net.xhttp.config;

import com.currency.net.base.joggle.IXSessionNotify;
import com.currency.net.xhttp.joggle.IXHttpConfig;
import com.currency.net.xhttp.joggle.IXHttpDns;
import com.currency.net.xhttp.joggle.IXHttpResponseConvert;

public class XHttpConfig implements IXHttpConfig {

    private IXHttpDns mDns = null;
    private IXSessionNotify mSessionNotify;
    private IXHttpResponseConvert mResponseConvert = null;

    @Override
    public void setXHttpDns(IXHttpDns dns) {
        this.mDns = dns;
    }

    @Override
    public IXHttpDns getXHttpDns() {
        return mDns;
    }

    @Override
    public void setResponseConvert(IXHttpResponseConvert responseConvert) {
        this.mResponseConvert = responseConvert;
    }

    @Override
    public void setSessionNotify(IXSessionNotify sessionNotify) {
        this.mSessionNotify = sessionNotify;
    }

    @Override
    public IXHttpResponseConvert getResponseConvert() {
        return mResponseConvert;
    }

    @Override
    public IXSessionNotify getSessionNotify() {
        return mSessionNotify;
    }

    public static XHttpConfig getDefaultConfig() {
        XHttpConfig httpConfig = new XHttpConfig();
        httpConfig.setXHttpDns(new XHttpDefaultDns());
        httpConfig.setSessionNotify(new XHttpDefaultSessionNotify());
        httpConfig.setResponseConvert(new XHttpDefaultResponseConvert());
        return httpConfig;
    }

}
