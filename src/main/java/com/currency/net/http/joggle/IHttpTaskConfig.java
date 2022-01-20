package com.currency.net.http.joggle;

import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.base.joggle.ISessionNotify;

import java.util.Map;

public interface IHttpTaskConfig {

    void setSessionNotify(ISessionNotify sessionNotify);

    void setConvertResult(IResponseConvert convertResult);

    void setInterceptRequest(IRequestIntercept interceptRequest);

    void setHttpSSLFactory(ISSLFactory factory);

    void setFreeExit(long millisecond);

    void setBaseUrl(String baseUrl);

    void setTimeout(int timeout);

    void setGlobalRequestProperty(Map<Object, Object> property);
}
