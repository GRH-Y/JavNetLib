package com.jav.net.base;

import com.jav.net.state.joggle.IState;

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
    public static final int INVALID = 0;

    /**
     * 任务正在结束
     */
    public static final int FINISHING = 1;

    /**
     * 任务还没开始
     */
    public static final int NONE = 2;

    /**
     * 任务已加载，等待执行
     */
    public static final int LOAD = 4;

    /**
     * 处理事件
     */
    public static final int RUN = 8;

    /**
     * 无事件状态
     */
    public static final int IDLING = 16;


    private volatile AtomicInteger mStatus = new AtomicInteger(2);

    @Override
    public AtomicInteger getStateEntity() {
        return mStatus;
    }


    @Override
    public String toString() {
        String name = "INVALID";
        switch (mStatus.get()) {
            case 1:
                name = "FINISH";
                break;
            case 2:
                name = "NONE";
                break;
            case 4:
                name = "LOAD";
                break;
            case 8:
                name = "RUN";
                break;
            case 16:
                name = "IDLING";
                break;
        }
        return "NetTaskStatus { code = " + mStatus + " name = " + name + " }";
    }
}
