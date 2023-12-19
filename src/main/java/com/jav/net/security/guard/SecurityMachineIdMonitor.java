package com.jav.net.security.guard;

import com.jav.common.log.LogDog;
import com.jav.net.security.channel.SecurityChannelContext;
import com.jav.net.security.channel.joggle.IServerChannelStatusListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityMachineIdMonitor {

    private final Map<String, MonitorChannel> mChannelCache = new HashMap<>();

    private static final int TWO_MIN = 2000 * 60;

    private static final int TEN_MIN = 1000 * 60 * 10;

    private static final int REG_TIME = 5000;


    private static final int MAX_TRY_REG_COUNT = 24;


    private static class MonitorChannel {

        private String mMachineId;

        private String mCurAddress;

        private int mChannelCount = 0;
        private long mRegChannelTime;

        private int mTryRegChannelCount = 0;

        private boolean mIsLock = false;


        private final List<IServerChannelStatusListener> mListener;


        MonitorChannel(String machineId, String curAddress) {
            mMachineId = machineId;
            this.mCurAddress = curAddress;
            mChannelCount++;
            mRegChannelTime = System.currentTimeMillis();
            mListener = new ArrayList<>(SecurityChannelContext.MAX_CHANNEL);
            LogDog.w("#Monitor# create MonitorChannel mid = " + mMachineId);
        }

        boolean checkAddress(String address) {
            if (address == null) {
                return false;
            }
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
            if (address.equals(mCurAddress)) {
                //匹配到当前地址
                if (System.currentTimeMillis() - mRegChannelTime > REG_TIME
                        || mChannelCount >= SecurityChannelContext.MAX_CHANNEL) {
                    //超过5秒，判定为新的连接，出现同个machineId是被占用或者新起的连接,需要断开之前的连接
                    String warring = String.format("#Monitor# curAddress: %s" +
                            " same machineId: %s is used, need to disconnect !!! ", mCurAddress, mMachineId);
                    LogDog.w(warring);
                    reset(address);
                } else {
                    mChannelCount++;
                    LogDog.w("#Monitor# ChannelCount = " + mChannelCount);
                }
            } else {
                //出现同个machineId是被占用或者新起的连接，需要断开之前的连接
                String warring = String.format("#Monitor# dif curAddress: %s newAddress: %s," +
                        " same machineId: %s is used, need to disconnect !!! ", mCurAddress, address, mMachineId);
                LogDog.w(warring);
                reset(address);
            }
            return true;
        }


        private void reset(String newAddress) {
            for (IServerChannelStatusListener listener : mListener) {
                listener.onRepeatMachine(mMachineId);
            }
            mListener.clear();
            mChannelCount = 1;
            mCurAddress = newAddress;

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
                mListener.add(listener);
                String warring = String.format("#Monitor# ChannelListener= %s , size= %d", listener, mListener.size());
                LogDog.w(warring);
            }
        }

        @Override
        public String toString() {
            return "---------------- MachineId=" + mMachineId + " ----------------\n" +
                    " CurAddress=" + mCurAddress + " \n" +
                    " ChannelCount=" + mChannelCount + " \n" +
                    " RegChannelTime=" + mRegChannelTime + " \n" +
                    " TryRegChannelCount=" + mTryRegChannelCount + " \n" +
                    " IsLock=" + mIsLock + " \n" +
                    "-------------------------------------------------------------\n";
        }
    }

    private SecurityMachineIdMonitor() {
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
     * @param address
     * @return
     */
    public boolean binderMachineIdForAddress(String machineId, String address) {
        synchronized (mChannelCache) {
            MonitorChannel monitorChannel = mChannelCache.get(machineId);
            if (monitorChannel == null) {
                monitorChannel = new MonitorChannel(machineId, address);
                mChannelCache.put(machineId, monitorChannel);
                return true;
            }
            return monitorChannel.checkAddress(address);
        }
    }

    /**
     * 设置machineId绑定冲突监听回调
     *
     * @param machineId
     * @param listener
     */
    public void setRepeatMachineListener(String machineId, IServerChannelStatusListener listener) {
        MonitorChannel monitorChannel = mChannelCache.get(machineId);
        monitorChannel.addRepeatMachineListener(listener);
    }


    /**
     * 校验 machineId 跟记录绑定的地址是否一致
     *
     * @param machineId machineId
     * @param address   客户端地址
     * @return
     */
    public boolean checkMachineIdForAddress(String machineId, String address) {
        if (machineId == null || address == null) {
            return false;
        }
        MonitorChannel monitorChannel = mChannelCache.get(machineId);
        return monitorChannel != null && monitorChannel.mCurAddress.equals(address);
    }

}
