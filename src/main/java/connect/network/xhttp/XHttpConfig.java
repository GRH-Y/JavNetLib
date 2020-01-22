package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.xhttp.joggle.IXHttpConfig;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import connect.network.xhttp.joggle.IXHttpResponseConvert;

public class XHttpConfig implements IXHttpConfig {

    private IXHttpDns dns = null;
    private AbsNetFactory factory = null;
    private IXHttpIntercept intercept = null;
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
    public void setIntercept(IXHttpIntercept intercept) {
        this.intercept = intercept;
    }

    @Override
    public IXHttpIntercept getIntercept() {
        return intercept;
    }

    public void setResponseConvert(IXHttpResponseConvert responseConvert) {
        this.responseConvert = responseConvert;
    }

    public IXHttpResponseConvert getResponseConvert() {
        return responseConvert;
    }

    @Override
    public void setNetFactory(AbsNetFactory factory) {
        if (factory == null) {
            return;
        }
        this.factory = factory;
    }

    @Override
    public AbsNetFactory getNetFactory() {
        return factory;
    }

}
