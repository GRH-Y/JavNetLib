package com.jav.net.security.guard;

import com.jav.common.log.LogDog;
import com.jav.net.security.channel.SecurityChannelContext;
import com.jav.net.security.channel.joggle.IServerChannelStatusListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SecurityMachineIdMonitor {

    private final Map<String, MonitorChannel> mChannelCache = new HashMap<>();

    private static final int TWO_MIN = 2000 * 60;

    private static final int TEN_MIN = 1000 * 60 * 10;


    private static final int MAX_TRY_REG_COUNT = 24;

    private final ReentrantLock mSelectLock;


    private static class MonitorChannel {

        private String mMachineId;

        private int mChannelCount = 0;
        private long mRegChannelTime;

        private int mTryRegChannelCount = 0;

        private boolean mIsLock = false;


        private final List<IServerChannelStatusListener> mListener;


        MonitorChannel(String machineId) {
            mMachineId = machineId;
            mChannelCount++;
            mRegChannelTime = System.currentTimeMillis();
            mListener = new ArrayList<>(SecurityChannelContext.MAX_CHANNEL);
            LogDog.w("#Monitor# create MonitorChannel mid = " + mMachineId);
        }

        boolean recordRegChannel() {
            if (mIsLock) {
                if (System.currentTimeMillis() - mRegChannelTime > TEN_MIN) {
                    //已经锁定machineId 超过10分钟，开始解锁
                    mIsLock = false;
                    mChannelCount = 0;
                    mTryRegChannelCount = 0;
                    LogDog.w("#Monitor# unLock machineId = " + mMachineId);
                }
                if (mIsLock) {
                    mRegChannelTime = System.currentTimeMillis();
                    return false;
                }
            }
            if (mChannelCount > SecurityChannelContext.MAX_CHANNEL) {
                //超过10秒，判定为新的连接，出现同个machineId是被占用或者新起的连接,需要断开之前的连接
                LogDog.w(String.format("#Monitor# same machineId: %s is used, need to disconnect !!!", mMachineId));
                reset();
            } else {
                mChannelCount++;
                mTryRegChannelCount = 0;
                LogDog.w("#Monitor# ChannelCount = " + mChannelCount);
            }
            return true;
        }


        private void reset() {
            synchronized (mListener) {
                for (IServerChannelStatusListener listener : mListener) {
                    listener.onRepeatMachine(mMachineId);
                }
                mListener.clear();
            }
            mChannelCount = 1;

            if (System.currentTimeMillis() - mRegChannelTime < TWO_MIN) {
                //2分钟内出现多次抢占注册machineId
                mTryRegChannelCount++;
                if (mTryRegChannelCount >= MAX_TRY_REG_COUNT) {
                    mIsLock = true;
                    LogDog.w("#Monitor# lock machineId = " + mMachineId);
                }
            } else {
                mTryRegChannelCount = 0;
            }
            mRegChannelTime = System.currentTimeMillis();
        }

        void addRepeatMachineListener(IServerChannelStatusListener listener) {
            if (listener != null) {
                synchronized (mListener) {
                    mListener.add(listener);
                }
                String warring = String.format("#Monitor# add ChannelListener= %s , size= %d", listener, mListener.size());
                LogDog.w(warring);
            }
        }

        void delRepeatMachineListener(IServerChannelStatusListener listener) {
            if (listener != null) {
                synchronized (mListener) {
                    mListener.remove(listener);
                }
                String warring = String.format("#Monitor# remove ChannelListener= %s , size= %d", listener, mListener.size());
                LogDog.w(warring);
            }
        }

        @Override
        public String toString() {
            return "---------------- MachineId=" + mMachineId + " ----------------\n" +
                    " ChannelCount=" + mChannelCount + " \n" +
                    " RegChannelTime=" + mRegChannelTime + " \n" +
                    " TryRegChannelCount=" + mTryRegChannelCount + " \n" +
                    " IsLock=" + mIsLock + " \n" +
                    "-------------------------------------------------------------\n";
        }
    }

    private SecurityMachineIdMonitor() {
        mSelectLock = new ReentrantLock(true);
    }

    private static class InnerCore {
        public static final SecurityMachineIdMonitor sMater = new SecurityMachineIdMonitor();
    }

    public static SecurityMachineIdMonitor getInstance() {
        return SecurityMachineIdMonitor.InnerCore.sMater;
    }

    /**
     * 绑定machineId跟地址对应
     *
     * @param machineId
     * @return
     */
    public boolean binderMachineIdForAddress(String machineId) {
        mSelectLock.lock();
        try {
            MonitorChannel monitorChannel = mChannelCache.get(machineId);
            if (monitorChannel == null) {
                monitorChannel = new MonitorChannel(machineId);
                mChannelCache.put(machineId, monitorChannel);
                return true;
            }
            return monitorChannel.recordRegChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            mSelectLock.unlock();
        }
        return false;
    }

    /**
     * 设置machineId绑定冲突监听回调
     *
     * @param machineId
     * @param listener
     */
    public void addRepeatMachineListener(String machineId, IServerChannelStatusListener listener) {
        MonitorChannel monitorChannel = mChannelCache.get(machineId);
        monitorChannel.addRepeatMachineListener(listener);
    }

    public void removeRepeatMachineListener(String machineId, IServerChannelStatusListener listener) {
        MonitorChannel monitorChannel = mChannelCache.get(machineId);
        monitorChannel.delRepeatMachineListener(listener);
    }


}
