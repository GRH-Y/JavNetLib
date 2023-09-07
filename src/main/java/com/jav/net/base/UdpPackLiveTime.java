package com.jav.net.base;

public enum UdpPackLiveTime {

    LOCAL_NETWORK(1),
    NETWORK_SITE(32),
    LOCAL_AREA(64),
    LOCAL_CONTINENT(128),
    EVERYWHERE(255);

    final int ttl;

    UdpPackLiveTime(int ttl) {
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
    }
}