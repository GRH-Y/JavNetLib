package com.jav.net.security.channel.joggle;


/**
 * sync服务通道状态监听器
 *
 * @author yyz
 */
public interface IServerSyncStatusListener  {

    /**
     * 获取machine Id
     *
     * @return
     */
    String getMachineId();

    /**
     * 同步machine id 结果回调
     *
     * @param status
     */
    void onSyncMinState(byte status);

}