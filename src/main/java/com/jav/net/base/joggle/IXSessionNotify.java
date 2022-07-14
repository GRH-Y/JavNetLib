package com.jav.net.base.joggle;


import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.entity.XResponse;

/**
 * 会话回调接口
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */

public interface IXSessionNotify {

    /**
     * 通知结果
     *
     * @param request
     */
    void notifyData(XRequest request, XResponse response, Throwable e);
}
