package com.currency.net.entity;


import java.util.concurrent.atomic.AtomicInteger;

public class NetTaskStatus {

    private final AtomicInteger mStatus = new AtomicInteger(0);

    public NetTaskStatus(NetTaskStatusCode code) {
        setCode(code);
    }

    public NetTaskStatusCode getCode() {
        return NetTaskStatusCode.getInstance(mStatus.get());
    }

    public void setCode(NetTaskStatusCode newCode) {
        mStatus.set(newCode.getCode());
    }

    public boolean updateCode(NetTaskStatusCode expectCode, NetTaskStatusCode setCode) {
        return mStatus.compareAndSet(expectCode.getCode(), setCode.getCode());
    }
}
