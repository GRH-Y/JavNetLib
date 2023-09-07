package com.jav.net.security.cache;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 缓存sync同步过来的machineId
 */
public class CacheExtMachineIdMater {

    /**
     * 缓存machine id
     */
    private final Set<String> mCacheMachine = new LinkedHashSet<>();


    private CacheExtMachineIdMater() {
    }

    private static class InnerCore {
        public static final CacheExtMachineIdMater sMater = new CacheExtMachineIdMater();
    }

    public static CacheExtMachineIdMater getInstance() {
        return InnerCore.sMater;
    }

    /**
     * 缓存mid
     *
     * @param mid 机器码
     * @return true为保存成功
     */
    public boolean cacheMid(String mid) {
        if (mid == null) {
            return false;
        }
        if (mCacheMachine.contains(mid)) {
            return true;
        }
        return mCacheMachine.add(mid);
    }

    /**
     * 检查mid是否存在
     *
     * @param mid 机器码
     * @return true为存在
     */
    public boolean checkMid(String mid) {
        for (String cacheMid : mCacheMachine) {
            if (cacheMid.equalsIgnoreCase(mid)) {
                return true;
            }
        }
        return false;
    }

}
