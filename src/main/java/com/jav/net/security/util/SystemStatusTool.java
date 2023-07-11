package com.jav.net.security.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class SystemStatusTool {

    private static final float RATIO_LOAD = 0.3f;

    private static final float RATIO_NET = 0.4f;

    private static final float RATIO_CPU = 0.3f;

    private SystemStatusTool() {
    }

    /**
     * 获取系统当前的负载情况
     *
     * @param loadCount
     * @return
     */
    public static byte getSystemAvgLoad(long loadCount) {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // 获取网络当前负载，单位为负载平均值
        double networkLoad = osBean.getSystemLoadAverage();
        // 获取CPU当前负载
        double cpuLoad = osBean.getSystemCpuLoad();
        return (byte) (loadCount * RATIO_LOAD + networkLoad * RATIO_NET + cpuLoad * RATIO_CPU);
    }

}
