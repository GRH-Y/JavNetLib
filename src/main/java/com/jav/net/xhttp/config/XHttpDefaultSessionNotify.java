package com.jav.net.xhttp.config;

import com.jav.common.util.ReflectionCall;
import com.jav.net.base.joggle.IXSessionNotify;
import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.entity.XResponse;

public class XHttpDefaultSessionNotify implements IXSessionNotify {

    @Override
    public void notifySuccess(XRequest request, XResponse response) {
        ReflectionCall.invoke(request.getCallBackTarget(), request.getCallBackSuccessMethod(),
                new Class[]{XRequest.class, XResponse.class}, request, response);
    }

    @Override
    public void notifyError(XRequest request, Throwable e) {
        ReflectionCall.invoke(request.getCallBackTarget(), request.getCallBackErrorMethod(),
                new Class[]{XRequest.class, Throwable.class}, request, e);
    }
}
