package connect.network.xhttp.config;

import connect.network.base.joggle.IXSessionNotify;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import util.ReflectionCall;

public class XHttpDefaultSessionNotify implements IXSessionNotify {

    @Override
    public void notifyData(XRequest request, XResponse response, Throwable e) {
        ReflectionCall.invoke(request.getCallBackTarget(), request.getCallBackMethod(),
                new Class[]{XRequest.class, XResponse.class, Throwable.class}, request, response, e);
    }
}
