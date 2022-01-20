package com.currency.net.http.joggle;

import com.currency.net.http.RequestEntity;

public interface IRequestIntercept {
    /**
     * 拦截请求(当前状态是准备发起请求)
     *
     * @return 返回true则需要拦截
     */
    boolean onStartRequestIntercept(RequestEntity submitEntity);

    /**
     * 拦截请求回调结果(当前状态是请求完成)
     *
     * @return 返回true则需要拦截
     */
    boolean onRequestInterceptResult(RequestEntity submitEntity);

}
