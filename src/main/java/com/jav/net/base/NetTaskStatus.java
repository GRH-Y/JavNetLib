package com.jav.net.base;


import com.jav.common.state.joggle.IState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络任务状态
 *
 * @author yyz
 */
public class NetTaskStatus implements IState<AtomicInteger> {

    /**
     * 任务完成已失效
     */
    public static final int INVALID = 1;

    /**
     * 任务正在结束
     */
    public static final int FINISHING = 2;

    /**
     * 任务还没开始
     */
    public static final int NONE = 4;

    /**
     * 任务已加载，等待执行
     */
    public static final int LOAD = 8;

    /**
     * 处理事件
     */
    public static final int RUN = 16;



    private final AtomicInteger mStatus = new AtomicInteger(NONE);

    @Override
    public AtomicInteger getStateEntity() {
        return mStatus;
    }


    @Override
    public String toString() {
        String name = "NULL";
        switch (mStatus.get()) {
            case INVALID:
                name = "INVALID";
                break;
            case FINISHING:
                name = "FINISHING";
                break;
            case NONE:
                name = "NONE";
                break;
            case LOAD:
                name = "LOAD";
                break;
            case RUN:
                name = "RUN";
                break;
        }
        return "NetTaskStatus { code = " + mStatus + " name = " + name + " }";
    }
}
