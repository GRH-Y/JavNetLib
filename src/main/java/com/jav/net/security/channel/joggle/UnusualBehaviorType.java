package com.jav.net.security.channel.joggle;

public enum UnusualBehaviorType {

    LENGTH("Illegal length"),
    TIME("Illegal Time"),
    MACHINE("Illegal Machine"),
    CHANNEL("Illegal Channel");

    private String mError;

    UnusualBehaviorType(String error) {
        mError = error;
    }

    public String getError() {
        return mError;
    }
}
