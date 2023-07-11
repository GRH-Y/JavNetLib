package com.jav.net.security.channel.joggle;

import com.jav.net.security.channel.base.UnusualBehaviorType;

/**
 * 安全策略接口
 *
 * @author yyz
 */
public interface ISecurityPolicyProcessor {


    /**
     * 校验时间,48小时时间差都有效
     *
     * @param time
     * @return 返回true为校验通过
     */
    boolean onCheckTime(long time);

    /**
     * 校验机器id
     *
     * @param checkMid 机器id
     * @return 返回true为校验通过
     */
    boolean onCheckMachineId(String checkMid);


    /**
     * 校验channel id
     *
     * @param channelId 通道id
     * @return true 为校验通过
     */
    boolean onCheckChannelId(String channelId);


    /**
     * 异常行为
     *
     * @param host
     * @param type
     */
    void onUnusualBehavior(String host, UnusualBehaviorType type);

}
