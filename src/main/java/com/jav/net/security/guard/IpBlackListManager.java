package com.jav.net.security.guard;

import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 管理ip黑名单
 *
 * @author yyz
 */
public class IpBlackListManager {

    private static final class InnerClass {
        public static final IpBlackListManager sManager = new IpBlackListManager();
    }

    public static IpBlackListManager getInstance() {
        return InnerClass.sManager;
    }

    /**
     * 确认目标的缓存
     */
    private LinkedList<String> mConfirmCache = new LinkedList<>();
    /**
     * 怀疑目标,有3次机会
     */
    private Map<String, ObserveEntity> mObserveCache = new LinkedHashMap<>();

    private class ObserveEntity {
        private String mHost;
        private int mCount = 1;

        private static final int sConfirmCount = 3;

        public ObserveEntity(String host) {
            mHost = host;
        }

        public int incCount() {
            mCount++;
            return mCount;
        }

        public boolean isReachCount() {
            return mCount > sConfirmCount;
        }

        public String getHost() {
            return mHost;
        }
    }

    private IpBlackListManager() {
    }

    protected byte[] getBlackList() {
        Object[] objects;
        synchronized (mConfirmCache) {
            objects = mConfirmCache.toArray();
        }
        if (objects == null || objects.length == 0) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Object obj : objects) {
            String item = (String) obj;
            try {
                stream.write(item.getBytes());
                stream.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stream.toByteArray();
    }

    public void addBlackList(String ip) {
        if (StringEnvoy.isEmpty(ip)) {
            return;
        }
        synchronized (mConfirmCache) {
            for (String text : mConfirmCache) {
                if (text.equals(ip)) {
                    return;
                }
            }
        }
        synchronized (mObserveCache) {
            ObserveEntity entity = mObserveCache.get(ip);
            if (entity == null) {
                entity = new ObserveEntity(ip);
                this.mObserveCache.put(ip, entity);
                LogDog.w("wary " + ip);
                return;
            }
            int count = entity.incCount();
            if (entity.isReachCount()) {
                mObserveCache.remove(ip);
            } else {
                LogDog.w("wary " + ip + " count = " + count);
                return;
            }
        }
        synchronized (mConfirmCache) {
            mConfirmCache.add(ip);
        }
        LogDog.w("add " + ip + " to the blacklist");
    }

    public void removeBlackList(String ip) {
        synchronized (mConfirmCache) {
            mConfirmCache.remove(ip);
        }
    }

    public boolean isBlackList(String ip) {
        synchronized (mConfirmCache) {
            for (String text : mConfirmCache) {
                if (text.equals(ip)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void clearBlackList() {
        synchronized (mConfirmCache) {
            mConfirmCache.clear();
        }
        synchronized (mObserveCache) {
            mObserveCache.clear();
        }
    }
}
