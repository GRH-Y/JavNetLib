package com.jav.net.security.channel.base;

public class ChannelTrafficInfo {

    private String mMachineId;

    private long mOutTrafficKB;

    private long mInTrafficKB;

    public ChannelTrafficInfo(String machineId) {
        mMachineId = machineId;
    }

    public void updateTraffic(long inTraffic, long outTraffic) {
        synchronized (this) {
            if (inTraffic != 0) {
                this.mInTrafficKB += inTraffic / 1024;
            }
            if (outTraffic != 0) {
                this.mOutTrafficKB += outTraffic / 1024;
            }
        }
    }

    public String getMachineId() {
        return mMachineId;
    }

    public long getInTrafficKB() {
        synchronized (this) {
            return mInTrafficKB;
        }
    }

    public long getOutTrafficKB() {
        synchronized (this) {
            return mOutTrafficKB;
        }
    }

    public void reset() {
        synchronized (this) {
            this.mInTrafficKB = 0;
            this.mOutTrafficKB = 0;
        }
    }

    @Override
    public String toString() {
        return "---------------- MachineId = " + mMachineId + " ----------------\n" +
                "                           Out Traffic = " + mOutTrafficKB + "kb\n" +
                "                           In Traffic = " + mInTrafficKB + "kb\n" +
                "------------------------------------------------------------------------------\n";
    }
}
