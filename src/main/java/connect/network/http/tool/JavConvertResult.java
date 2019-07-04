package connect.network.http.tool;

import connect.network.http.joggle.IResponseConvert;
import json.JsonEnvoy;
import util.GZipUtils;
import util.LogDog;

public class JavConvertResult implements IResponseConvert {

    @Override
    public Object handlerEntity(Class resultCls, byte[] result, String encode) {
        if (resultCls == null || result == null) {
            return null;
        }
        byte[] newData = result;
        if ("gzip".equals(encode)) {
            byte[] unCompressData = GZipUtils.unCompress(result);
            newData = unCompressData == null ? result : unCompressData;
        }

        if (resultCls.isAssignableFrom(byte[].class)) {
            return newData;
        }
        String resultStr = new String(newData);
        LogDog.d("==> Request to return the content = " + resultStr);
        return JsonEnvoy.toEntity(resultCls, resultStr);
    }
}
