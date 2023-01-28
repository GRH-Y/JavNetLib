package com.jav.net.security.channel.joggle;

/**
 * sync 服务事件回调
 *
 * @author yyz
 */
public interface ISyncServerEventCallBack {

    /**
     * 同步负载回调
     *
     * @param status    1为请求，2为响应
     * @param proxyPort 远程代理服务的端口
     * @param machineId 机器id
     * @param loadCount 负载值
     */
    void onRespondSyncCallBack(byte status, int proxyPort, String machineId, long loadCount);
}
