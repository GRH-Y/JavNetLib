package connect.network.xhttp.joggle;

import connect.network.base.AbsNetFactory;

public interface IXHttpConfig {

    void setXHttpDns(IXHttpDns dns);

    IXHttpDns getXHttpDns();

    void setIntercept(IXHttpIntercept intercept);

    IXHttpIntercept getIntercept();

    void setResponseConvert(IXHttpResponseConvert responseConvert);

    IXHttpResponseConvert getResponseConvert();

    void setNetFactory(AbsNetFactory factory);

    AbsNetFactory getNetFactory();

}
