package connect.network.http.joggle;

import connect.network.base.joggle.ISessionCallBack;

public interface IHttpTaskConfig {

    void setSessionCallBack(ISessionCallBack sessionCallBack);

    void setConvertResult(IConvertResult convertResult);

    void setInterceptRequest(IInterceptRequest interceptRequest);

    void setFreeExit(long millisecond);

    void setBaseUrl(String baseUrl);
}
