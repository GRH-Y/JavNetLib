package com.currency.net.xhttp.config;

import com.currency.net.base.joggle.IXSessionNotify;
import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.entity.XResponse;
import util.ReflectionCall;

public class XHttpDefaultSessionNotify implements IXSessionNotify {

    @Override
    public void notifyData(XRequest request, XResponse response, Throwable e) {
        ReflectionCall.invoke(request.getCallBackTarget(), request.getCallBackMethod(),
                new Class[]{XRequest.class, XResponse.class, Throwable.class}, request, response, e);
    }
}
