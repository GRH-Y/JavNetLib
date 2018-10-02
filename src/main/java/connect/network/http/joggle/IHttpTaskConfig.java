package connect.network.http.joggle;

import connect.network.base.joggle.ISessionCallBack;

public interface IHttpTaskConfig {

    void setSessionCallBack(ISessionCallBack sessionCallBack);

    void setConvertResult(IResponseConvert convertResult);

    void setInterceptRequest(IRequestIntercept interceptRequest);

    void setHttpSSLFactory(IHttpSSLFactory factory);

    void setFreeExit(long millisecond);

    void setBaseUrl(String baseUrl);
}
