package connect.network.xhttp;

import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.entity.XHttpResponse;
import connect.network.xhttp.joggle.IXHttpResponseConvert;

public class XHttpDefaultResponseConvert implements IXHttpResponseConvert {

    @Override
    public Object handlerEntity(XHttpRequest request, XHttpResponse response) {
        Object result = request.getResultType();
        if (result instanceof String) {

        } else if (result instanceof Class) {

        }
        return null;
    }
}
