package connect.network.xhttp;

import connect.network.xhttp.joggle.IXHttpConfig;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;

public class XHttpConfig implements IXHttpConfig {

    private IXHttpDns dns;
    private IXHttpIntercept intercept;

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
}
