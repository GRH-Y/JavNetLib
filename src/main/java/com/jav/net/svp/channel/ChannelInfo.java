package com.jav.net.svp.channel;

import java.net.SocketAddress;

public class ChannelInfo {

    private boolean mIsTcp;
    private String mPassId;

    private short mRequestId;

    private long mPackId;

    private short mPackCount;

    private SocketAddress mSourceAddress;

    private SocketAddress mTargetAddress;

    public void setTcp(boolean isTcp) {
        this.mIsTcp = isTcp;
    }

    public boolean isTcp() {
        return mIsTcp;
    }

    public String getPassId() {
        return mPassId;
    }

    public void setPassId(String passId) {
        this.mPassId = passId;
    }

    public short getRequestId() {
        return mRequestId;
    }

    public void setRequestId(short requestId) {
        this.mRequestId = requestId;
    }

    public long getPackId() {
        return mPackId;
    }

    public void setPackId(long packId) {
        this.mPackId = packId;
    }

    public short getPackCount() {
        return mPackCount;
    }

    public void setPackCount(short packCount) {
        this.mPackCount = packCount;
    }

    public SocketAddress getSourceAddress() {
        return mSourceAddress;
    }

    public void setSourceAddress(SocketAddress sourceAddress) {
        this.mSourceAddress = sourceAddress;
    }

    public SocketAddress getTargetAddress() {
        return mTargetAddress;
    }

    public void setTargetAddress(SocketAddress targetAddress) {
        this.mTargetAddress = targetAddress;
    }
}
