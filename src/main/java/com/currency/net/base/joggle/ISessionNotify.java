package com.currency.net.base.joggle;


import com.currency.net.http.RequestEntity;

/**
 * 会话回调接口
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */

public interface ISessionNotify {

    /**
     * 通知结果
     *
     * @param request
     */
    void notifyData(RequestEntity request);

    /**
     * 通知处理过程状态
     *
     * @param request
     * @param bytesRead     已读内容大小
     * @param contentLength 内容的大小
     */
    void notifyProcess(RequestEntity request, int bytesRead, int contentLength);
}
