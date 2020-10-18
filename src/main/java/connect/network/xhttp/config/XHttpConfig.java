package connect.network.xhttp.config;

import connect.network.base.joggle.IXSessionNotify;
import connect.network.xhttp.joggle.IXHttpConfig;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpResponseConvert;

public class XHttpConfig implements IXHttpConfig {

    private IXHttpDns dns = null;
    private IXSessionNotify mSessionNotify;
    private IXHttpResponseConvert responseConvert = null;

    @Override
    public void setXHttpDns(IXHttpDns dns) {
        this.dns = dns;
    }

    @Override
    public IXHttpDns getXHttpDns() {
        return dns;
    }

    @Override
    public void setResponseConvert(IXHttpResponseConvert responseConvert) {
        this.responseConvert = responseConvert;
    }

    @Override
    public void setSessionNotify(IXSessionNotify sessionNotify) {
        this.mSessionNotify = sessionNotify;
    }

    @Override
    public IXHttpResponseConvert getResponseConvert() {
        return responseConvert;
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
