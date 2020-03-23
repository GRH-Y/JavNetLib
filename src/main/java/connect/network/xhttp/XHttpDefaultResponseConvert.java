package connect.network.xhttp;

import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import json.JsonEnvoy;
import storage.GZipUtils;
import util.StringEnvoy;

public class XHttpDefaultResponseConvert implements IXHttpResponseConvert {

    @Override
    public void handlerEntity(XRequest request, XResponse response) {
        String encode = response.getHeadForKey(XHttpProtocol.XY_CONTENT_ENCODING);
        String transfer = response.getHeadForKey(XHttpProtocol.XY_TRANSFER_ENCODING);
        if (StringEnvoy.isNotEmpty(transfer) && "chunked".equals(transfer)) {
            //有分段,数据交给上层处理
            return;
        }
        if (StringEnvoy.isNotEmpty(encode) && encode.contains("gzip")) {
            //需要解压
            response.setHttpData(GZipUtils.unCompress(response.getHttpData()));
        }
        Object resultType = request.getResultType();
        if (resultType instanceof Class && !isBasicDataType((Class) resultType)) {
            response.setResult(JsonEnvoy.toEntity((Class) resultType.getClass(), new String(response.getHttpData())));
        }
    }

    private boolean isBasicDataType(Class clx) {
        return clx == Integer.class || clx == int.class || clx == int[].class
                || clx == Long.class || clx == long.class || clx == long[].class
                || clx == Double.class || clx == double.class || clx == double[].class
                || clx == Float.class || clx == float.class || clx == float[].class
                || clx == Boolean.class || clx == boolean.class || clx == boolean[].class
                || clx == Character.class || clx == char.class || clx == char[].class
                || clx == Byte.class || clx == byte.class || clx == byte[].class
                || clx == String.class;
    }
}
