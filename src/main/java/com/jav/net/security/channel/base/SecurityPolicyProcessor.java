package com.jav.net.security.channel.base;

import com.jav.common.security.Md5Helper;
import com.jav.net.security.channel.SecurityChannelContext;
import com.jav.net.security.channel.joggle.ISecurityPolicyProcessor;
import com.jav.net.security.channel.joggle.UnusualBehaviorType;
import com.jav.net.security.guard.IpBlackListManager;

import java.util.*;

/**
 * 安全策略处理器,主要是处理协议中异常的数据,记录异常的目标ip并拦截
 *
 * @author yyz
 */
public class SecurityPolicyProcessor implements ISecurityPolicyProcessor {

    private SecurityChannelContext mContext;

    private final Map<String, RecordChannel> mCacheChannelId;

    private static final int ONE_DAY = 1000 * 60 * 60 * 24;


    /**
     * 记录通道请求channelId和请求时间
     */
    private static class RecordChannel {

        private final List<String> mChannelList = new ArrayList<>(4);
        private long mTime;

        /**
         * 记录 channelId
         *
         * @param channelId
         */
        public void recodeChannelId(String channelId) {
            if (isValidChannelId(channelId)) {
                return;
            }
            mChannelList.add(channelId);
        }

        public void reset() {
            mChannelList.clear();
            mTime = System.currentTimeMillis();
        }

        public boolean isValidChannelId(String channelId) {
            for (String id : mChannelList) {
                if (id.equals(channelId)) {
                    return true;
                }
            }
            return false;
        }

        public void updateTime() {
            mTime = System.currentTimeMillis();
        }

        public boolean isOvertime() {
            return System.currentTimeMillis() - mTime > ONE_DAY;
        }
    }

    public SecurityPolicyProcessor(SecurityChannelContext context) {
        mContext = context;
        mCacheChannelId = new HashMap<>();
    }


    /**
     * 创建通道id
     *
     * @return 返回通道id
     */
    @Override
    public String createChannelId(String machineId) {
        String uuidStr = UUID.randomUUID().toString();
        String channelId = Md5Helper.md5_32(uuidStr);
        RecordChannel recordConnect = mCacheChannelId.get(machineId);
        if (recordConnect == null) {
            recordConnect = new RecordChannel();
            mCacheChannelId.put(machineId, recordConnect);
        } else {
            if (recordConnect.isOvertime()) {
                recordConnect.reset();
            }
        }
        recordConnect.recodeChannelId(channelId);
        return channelId;
    }


    @Override
    public boolean onCheckTime(long time) {
        long nowTime = System.currentTimeMillis();
        // 172800000 48小时的毫秒值
        return Math.abs(nowTime - time) < 172800000;
    }

    @Override
    public boolean onCheckMachineId(String checkMid) {
        if (mContext.isServerMode()) {
            // 检查mid是否合法
            List<String> machineList = mContext.getMachineList();
            if (machineList != null) {
                for (String mid : machineList) {
                    if (mid.equalsIgnoreCase(checkMid)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCheckChannelId(String channelId) {
        if (mContext.isServerMode()) {
            Collection<RecordChannel> collection = mCacheChannelId.values();
            for (RecordChannel recordConnect : collection) {
                if (recordConnect.isValidChannelId(channelId)) {
                    recordConnect.updateTime();
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void onUnusualBehavior(String host, UnusualBehaviorType type) {
        IpBlackListManager.getInstance().addBlackList(host);
    }
}
