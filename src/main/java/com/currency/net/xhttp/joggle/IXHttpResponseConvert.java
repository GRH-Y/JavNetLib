package com.currency.net.xhttp.joggle;

import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.entity.XResponse;

public interface IXHttpResponseConvert {

    /**
     * 处理请求返回的数据
     *
     * @param request  请求体
     * @param response 请求返回的数据
     * @return
     */
    void handlerEntity(XRequest request, XResponse response);
}
