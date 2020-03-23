package connect.network.xhttp.joggle;

public interface IXHttpConfig {

    void setXHttpDns(IXHttpDns dns);

    IXHttpDns getXHttpDns();

    void setIntercept(IXHttpIntercept intercept);

    IXHttpIntercept getIntercept();

    void setResponseConvert(IXHttpResponseConvert responseConvert);

    IXHttpResponseConvert getResponseConvert();

}
