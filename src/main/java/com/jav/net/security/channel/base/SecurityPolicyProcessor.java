package com.jav.net.security.channel.base;

import com.jav.net.security.cache.CacheChannelIdMater;
import com.jav.net.security.cache.CacheExtMachineIdMater;
import com.jav.net.security.channel.SecurityChannelContext;
import com.jav.net.security.channel.joggle.ISecurityPolicyProcessor;
import com.jav.net.security.guard.IpBlackListManager;

import java.util.List;

/**
 * 安全策略处理器,主要是处理协议中异常的数据,记录异常的目标ip并拦截
 *
 * @author yyz
 */
public class SecurityPolicyProcessor implements ISecurityPolicyProcessor {

    private SecurityChannelContext mContext;


    /**
     * 172800000 48小时的毫秒值
     */
    private static final int TWO_DAY = 1000 * 60 * 60 * 24 * 2;


    public SecurityPolicyProcessor(SecurityChannelContext context) {
        mContext = context;
    }


    @Override
    public boolean onCheckTime(long time) {
        long nowTime = System.currentTimeMillis();
        // 172800000 48小时的毫秒值
        return Math.abs(nowTime - time) < TWO_DAY;
    }

    @Override
    public boolean onCheckMachineId(String checkMid) {
        if (mContext.isServerMode()) {
            if (CacheExtMachineIdMater.getInstance().checkMid(checkMid)) {
                //匹配上分布式的缓存库中的machineId
                return true;
            }
            // 检查mid是否合法
            List<String> machineList = mContext.getMachineList();
            if (machineList == null || machineList.isEmpty()) {
                // mid列表为空默认放行所有
                return true;
            }
            for (String mid : machineList) {
                if (mid.equalsIgnoreCase(checkMid)) {
                    return true;
                }
            }
            return false;
        }
        //客户端模式 校验是不是本机的machine id
        return checkMid.equalsIgnoreCase(mContext.getMachineId());
    }

    @Override
    public boolean onCheckChannelId(String channelId) {
        if (mContext.isServerMode()) {
            return CacheChannelIdMater.getInstance().checkChannelId(channelId);
        }
        return true;
    }

    @Override
    public void onUnusualBehavior(String host, UnusualBehaviorType type) {
        IpBlackListManager.getInstance().addBlackList(host);
    }
}
