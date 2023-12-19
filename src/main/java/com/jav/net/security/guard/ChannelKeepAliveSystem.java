package com.jav.net.security.guard;

import com.jav.common.log.LogDog;
import com.jav.net.security.channel.joggle.ISecurityProxySender;
import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.LoopTaskExecutor;
import com.jav.thread.executor.TaskContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChannelKeepAliveSystem {
    private static final int ONE_MIN = 60 * 1000;

    private String mMachineId;

    private KeepAliveTimer mKeepAliveTimer;

    private final Map<String, TimerEntity> mChannelCache = new HashMap<>();

    private static class InnerCore {
        public static final ChannelKeepAliveSystem sSystem = new ChannelKeepAliveSystem();
    }

    public static ChannelKeepAliveSystem getInstance() {
        return ChannelKeepAliveSystem.InnerCore.sSystem;
    }


    private class TimerEntity {

        private ISecurityProxySender mSender;

        private long mTriggerTime;

        TimerEntity(ISecurityProxySender sender) {
            mSender = sender;
            mTriggerTime = System.currentTimeMillis();
        }

        public long getTriggerTime() {
            return mTriggerTime;
        }

        public void updateTriggerTime() {
            mTriggerTime = System.currentTimeMillis();
        }

        public ISecurityProxySender getSender() {
            return mSender;
        }
    }

    private class KeepAliveTimer extends LoopTask {


        private TaskContainer mTaskContainer;

        @Override
        protected void onRunLoopTask() {
            LoopTaskExecutor executor = mTaskContainer.getTaskExecutor();
            executor.waitTask(ONE_MIN);

            synchronized (KeepAliveTimer.class) {
                Collection<TimerEntity> collection = mChannelCache.values();
                Iterator<TimerEntity> iterator = collection.iterator();
                while (iterator.hasNext()) {
                    TimerEntity entity = iterator.next();
                    long lastTriggerTime = entity.getTriggerTime();
                    if (System.currentTimeMillis() - lastTriggerTime >= ONE_MIN) {
                        ISecurityProxySender sender = entity.getSender();
                        sender.sendKeepAlive(mMachineId);
                        entity.updateTriggerTime();
                        LogDog.i("#KA# trigger keep alive !");
                    }
                }
            }
        }

        public void startTimer() {
            if (mTaskContainer == null) {
                mTaskContainer = new TaskContainer(this);
                LoopTaskExecutor executor = mTaskContainer.getTaskExecutor();
                executor.startTask();
                LogDog.i("#KA# start keep alive timer !");
            }
        }

        public void stopTimer() {
            if (mTaskContainer != null) {
                LoopTaskExecutor executor = mTaskContainer.getTaskExecutor();
                executor.stopTask();
                LogDog.i("#KA# stop keep alive timer !");
            }
        }
    }

    private ChannelKeepAliveSystem() {
    }


    public void init(String machineId) {
        mMachineId = machineId;
        if (mKeepAliveTimer == null) {
            mKeepAliveTimer = new KeepAliveTimer();
            mKeepAliveTimer.startTimer();
        }
    }

    public void triggerKeepAlive(String channelId) {
        TimerEntity entity = mChannelCache.get(channelId);
        if (entity != null) {
            entity.updateTriggerTime();
        }
    }

    public void addMonitorChannel(String channelId, ISecurityProxySender sender) {
        synchronized (KeepAliveTimer.class) {
            mChannelCache.put(channelId, new TimerEntity(sender));
        }
    }

    public void removeMonitorChannel(String channelId) {
        synchronized (KeepAliveTimer.class) {
            mChannelCache.remove(channelId);
        }
    }


    public void destroy() {
        if (mKeepAliveTimer != null) {
            mKeepAliveTimer.stopTimer();
        }
    }

}
