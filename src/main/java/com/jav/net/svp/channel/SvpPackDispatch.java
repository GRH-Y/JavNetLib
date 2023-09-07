package com.jav.net.svp.channel;

import com.jav.net.base.UdpPacket;
import com.jav.net.base.joggle.INetReceiver;
import com.jav.net.svp.channel.joggle.ICombinedPackCompleteListener;
import com.jav.thread.executor.ConsumerListAttribute;
import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.LoopTaskExecutor;
import com.jav.thread.executor.TaskContainer;

/**
 * svp 数据包调度
 */
public class SvpPackDispatch extends LoopTask implements INetReceiver<UdpPacket> {

    private final ConsumerListAttribute<UdpPacket> mCacheQueues;
    private TaskContainer mDispatchTask;

    /**
     * 记录
     */
    private final SvpPackCombined mPackCombined;


    public SvpPackDispatch(ICombinedPackCompleteListener listener) {
        mCacheQueues = new ConsumerListAttribute<>();
        mPackCombined = new SvpPackCombined(listener);
    }


    @Override
    protected void onRunLoopTask() {
        UdpPacket packet = mCacheQueues.popCacheData();
        if (packet == null) {
            synchronized (mCacheQueues) {
                try {
                    mCacheQueues.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mPackCombined.combinedPack(packet);
        }
    }

    public void startDispatch() {
        if (mDispatchTask == null) {
            mDispatchTask = new TaskContainer(this);
            LoopTaskExecutor executor = mDispatchTask.getTaskExecutor();
            executor.startTask();
        }
    }

    public void stopDispatch() {
        if (mDispatchTask != null) {
            LoopTaskExecutor executor = mDispatchTask.getTaskExecutor();
            executor.stopTask();
            mDispatchTask = null;
            notifyContinue();
        }
    }

    private void notifyContinue() {
        synchronized (mCacheQueues) {
            mCacheQueues.notifyAll();
        }
    }

    @Override
    public void onReceiveFullData(UdpPacket packet) {
        try {
            mCacheQueues.pushToCache(packet);
            notifyContinue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveError(Throwable e) {
    }
}
