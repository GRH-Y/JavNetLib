package connect.network.xhttp.joggle;

import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;

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
