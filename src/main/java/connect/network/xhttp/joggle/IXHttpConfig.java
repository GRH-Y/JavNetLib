package connect.network.xhttp.joggle;

import connect.network.base.joggle.IXSessionNotify;

public interface IXHttpConfig {

    void setXHttpDns(IXHttpDns dns);

    IXHttpDns getXHttpDns();

    void setResponseConvert(IXHttpResponseConvert responseConvert);

    IXHttpResponseConvert getResponseConvert();

    void setSessionNotify(IXSessionNotify sessionNotify);

    IXSessionNotify getSessionNotify();

}
