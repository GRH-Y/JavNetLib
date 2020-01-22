package connect.network.xhttp.joggle;

import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.entity.XHttpResponse;

public interface IXHttpResponseConvert {

    /**
     * 处理请求返回的数据
     *
     * @param request  请求体
     * @param response 请求返回的数据
     * @return
     */
    Object handlerEntity(XHttpRequest request, XHttpResponse response);
}
