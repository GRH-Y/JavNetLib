package com.jav.net.xhttp.joggle;

import com.jav.net.base.joggle.IXSessionNotify;

public interface IXHttpConfig {

    void setXHttpDns(IXHttpDns dns);

    IXHttpDns getXHttpDns();

    void setResponseConvert(IXHttpResponseConvert responseConvert);

    IXHttpResponseConvert getResponseConvert();

    void setSessionNotify(IXSessionNotify sessionNotify);

    IXSessionNotify getSessionNotify();

}
