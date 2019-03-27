package connect.network.http.joggle;

import connect.network.base.joggle.ISSLFactory;
import connect.network.base.joggle.ISessionCallBack;

import java.util.Map;

public interface IHttpTaskConfig {

    void setSessionCallBack(ISessionCallBack sessionCallBack);

    void setConvertResult(IResponseConvert convertResult);

    void setInterceptRequest(IRequestIntercept interceptRequest);

    void setHttpSSLFactory(ISSLFactory factory);

    void setFreeExit(long millisecond);

    void setBaseUrl(String baseUrl);

    void setTimeout(int timeout);

    void setGlobalRequestProperty(Map<String, Object> property);
}
