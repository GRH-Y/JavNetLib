package com.jav.net.xhttp.entity;

public enum XHttpCode {

    OK(200), REDIRECT(300), NOT_FOUND(400), INTERNAL_ERROR(500);

    private int mCode;

    XHttpCode(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
