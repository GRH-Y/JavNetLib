package connect.network.http.joggle;

import connect.network.base.RequestEntity;

public interface IInterceptRequest {
    /**
     * 拦截请求(已请求返回，但没有处理请求返回结果)
     *
     * @return 返回true则需要拦截
     */
    boolean intercept(RequestEntity submitEntity);

    /**
     * 拦截请求回调结果(已处理返回结果，但没有回调返回结果)
     *
     * @return 返回true则需要拦截
     */
    boolean interceptCallBack(RequestEntity submitEntity, Object entity);
}
