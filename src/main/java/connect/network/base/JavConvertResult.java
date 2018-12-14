package connect.network.base;

import connect.network.http.joggle.IResponseConvert;
import json.JsonUtils;
import util.LogDog;

public class JavConvertResult implements IResponseConvert {

    @Override
    public Object handlerEntity(Class resultCls, byte[] result) {
        String jsonStr = new String(result);
        LogDog.d("==> Request to return the content = " + jsonStr);

        if (resultCls.isAssignableFrom(byte[].class)) {
            return result;
        }
        return JsonUtils.toEntity(resultCls, jsonStr);
    }
}
