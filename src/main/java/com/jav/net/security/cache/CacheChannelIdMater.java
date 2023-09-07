package com.jav.net.security.cache;

import com.jav.common.log.LogDog;
import com.jav.net.security.channel.SecurityChannelContext;

import java.io.Serializable;
import java.util.*;


/**
 * 缓存channel
 */
public class CacheChannelIdMater {

    private final Map<String, RecordChannel> mChannelCache = new HashMap<>();

    /**
     * 一天的毫秒值
     */
    private static final int ONE_DAY = 1000 * 60 * 60 * 24;

    private final Object mLock = new Object() {
        @Override
        public int hashCode() {
            return (int) System.nanoTime();
        }
    };


    /**
     * 记录通道请求channelId和请求时间
     */
    private static class RecordChannel implements Serializable {

        private final List<String> mChannelList = new LinkedList<>();


        /**
         * 记录channel访问的当前时间
         */
        private long mTime = System.currentTimeMillis();

        /**
         * 记录 channelId
         *
         * @param channelId
         */
        public void recodeChannelId(String channelId) {
            if (isValidChannelId(channelId)) {
                return;
            }
            if (mChannelList.size() >= SecurityChannelContext.MAX_CHANNEL) {
                String clearChannelId = mChannelList.remove(0);
                LogDog.w("## channelId cache full size, clear channelId = " + clearChannelId);
            }
            mChannelList.add(channelId);
        }

        public void reset() {
            mChannelList.clear();
            mTime = System.currentTimeMillis();
        }

        public void removeChannelId(String channelId) {
            mChannelList.remove(channelId);
        }

        public boolean isValidChannelId(String channelId) {
            for (String id : mChannelList) {
                if (id.equalsIgnoreCase(channelId)) {
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

    private CacheChannelIdMater() {
    }

    private static class InnerCore {
        public static final CacheChannelIdMater sMater = new CacheChannelIdMater();
    }

    public static CacheChannelIdMater getInstance() {
        return InnerCore.sMater;
    }

    /**
     * 绑定channel到指定的machineId
     *
     * @param machineId
     * @param channelId
     */
    public void binderChannelIdToMid(String machineId, String channelId) {
        synchronized (mLock) {
            RecordChannel recordChannel = mChannelCache.get(machineId);
            if (recordChannel == null) {
                recordChannel = new RecordChannel();
                mChannelCache.put(machineId, recordChannel);
            } else {
                if (recordChannel.isOvertime()) {
                    recordChannel.reset();
                    LogDog.w("## channelId over time !");
                }
            }
            recordChannel.recodeChannelId(channelId);
        }
    }

    /**
     * 检查channelId是否存在
     *
     * @param channelId
     * @return
     */
    public boolean checkChannelId(String channelId) {
        Collection<RecordChannel> collection = mChannelCache.values();
        for (RecordChannel recordConnect : collection) {
            if (recordConnect.isValidChannelId(channelId)) {
                recordConnect.updateTime();
                return true;
            }
        }
        return false;
    }

    /**
     * 作废指定的channelId
     *
     * @param channelId
     */
    public void repealChannelId(String machineId, String channelId) {
        if (machineId == null || channelId == null) {
            return;
        }
        synchronized (mLock) {
            RecordChannel recordChannel = mChannelCache.get(machineId);
            if (recordChannel != null) {
                recordChannel.removeChannelId(channelId);
            }
        }
    }
}
