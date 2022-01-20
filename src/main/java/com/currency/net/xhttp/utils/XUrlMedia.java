package com.currency.net.xhttp.utils;

import log.LogDog;
import util.StringEnvoy;

public class XUrlMedia {

    private String mUrl;
    private String mProtocol;
    private String mReferer = null;
    private String mHost = null;
    private String mPath = "/";
    private int mPort = 0;
    private boolean mIsTSL = false;

    public XUrlMedia(String url) {
        reset(url, -1);
    }

    public XUrlMedia(String url, int userPort) {
        reset(url, userPort);
    }

    public void reset(String url) {
        reset(url, -1);
    }

    public void reset(String url, int userPort) {
        if (StringEnvoy.isEmpty(url)) {
            throw new NullPointerException("url is null !!!");
        }
        int httpIndex = url.indexOf("://");
        if (httpIndex == -1) {
            throw new IllegalArgumentException("no protocol : " + url);
        }
        this.mProtocol = url.substring(0, httpIndex);
        this.mIsTSL = "https".equalsIgnoreCase(this.mProtocol);
        httpIndex += 3;
        int index = url.indexOf("/", httpIndex);
        if (index > 0) {
            this.mPath = url.substring(index);
        } else {
            index = url.length();
        }

        int portIndex = url.indexOf(":", httpIndex);
        if (userPort == -1) {
            if (portIndex == -1 || portIndex > index) {
                this.mPort = mIsTSL ? 443 : 80;
            } else {
                int endIndex = index == -1 ? url.length() : index;
                try {
                    this.mPort = Integer.parseInt(url.substring(portIndex + 1, endIndex));
                } catch (Exception e) {
                    LogDog.e(" url = " + url);
                    e.printStackTrace();
                }
            }
        } else {
            this.mPort = userPort;
        }
        this.mHost = url.substring(httpIndex, portIndex == -1 || portIndex > index ? index : portIndex);
        this.mReferer = url.substring(0, index) + "/";
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getHost() {
        return mHost;
    }

    public String getPath() {
        return mPath;
    }

    public String getReferer() {
        return mReferer;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isTSL() {
        return mIsTSL;
    }
}
