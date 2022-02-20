package com.currency.net.udp;

public enum LiveTime {

    LOCAL_NETWORK(1), NETWORK_SITE(32), LOCAL_AREA(64), LOCAL_CONTINENT(128), EVERYWHERE(255);

    final int ttl;

    LiveTime(int ttl) {
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
    }
}
