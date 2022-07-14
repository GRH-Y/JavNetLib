package com.jav.net.xhttp.config;

import com.jav.common.util.ReflectionCall;
import com.jav.net.base.joggle.IXSessionNotify;
import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.entity.XResponse;

public class XHttpDefaultSessionNotify implements IXSessionNotify {

    @Override
    public void notifyData(XRequest request, XResponse response, Throwable e) {
        ReflectionCall.invoke(request.getCallBackTarget(), request.getCallBackMethod(),
                new Class[]{XRequest.class, XResponse.class, Throwable.class}, request, response, e);
    }
}
