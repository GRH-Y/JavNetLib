package connect.network.http.joggle;

public interface IResponseConvert {

    /**
     * 处理请求返回的数据
     *
     * @param resultCls 数据转换的实体类
     * @param result    请求返回的数据
     * @param  encode 返回数据的编码类型
     * @return
     */
    Object handlerEntity(Class resultCls, byte[] result, String encode);
}
