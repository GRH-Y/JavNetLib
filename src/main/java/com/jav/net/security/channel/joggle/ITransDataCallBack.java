package com.jav.net.security.channel.joggle;

/**
 * 转发数据监听器
 *
 * @author yyz
 */
public interface ITransDataCallBack {

    /**
     * 转发数据解析回调
     *
     * @param requestId 请求id
     * @param pctCount  作用于udp 传输数据 记录数据包的顺序
     * @param data      转发的数据
     */
    void onTransData(String requestId, byte pctCount, byte[] data);


}
