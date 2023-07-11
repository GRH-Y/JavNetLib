package com.jav.net.security.channel.joggle;

import com.jav.net.security.channel.base.UnusualBehaviorType;

import java.util.Map;

/**
 * 通道事件回调
 *
 * @author yyz
 */
public interface IChannelEventCallBack {

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param data      转发的数据
     */
    void onTransData(String requestId, byte[] data);


    /**
     * 回调错误信息
     *
     * @param error
     * @param extData
     */
    void onChannelError(UnusualBehaviorType error, Map<String, String> extData);

}
