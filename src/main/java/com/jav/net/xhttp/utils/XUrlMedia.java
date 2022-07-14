package com.jav.net.xhttp.utils;


import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;

public class XUrlMedia {

    private static final String HTTPS = "https";
    private static final String URL_LOCATOR_SEPARATOR = "://";
    private static final String URL_SEPARATOR = "/";
    private static final String URL_COLON = ":";

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
        int httpIndex = url.indexOf(URL_LOCATOR_SEPARATOR);
        if (httpIndex == -1) {
            throw new IllegalArgumentException("no protocol : " + url);
        }
        //解析协议
        this.mProtocol = url.substring(0, httpIndex);
        //解析是否https
        this.mIsTSL = HTTPS.equalsIgnoreCase(this.mProtocol);

        httpIndex += 3;
        int index = url.indexOf(URL_SEPARATOR, httpIndex);
        if (index > 0) {
            this.mPath = url.substring(index);
        } else {
            index = url.length();
        }

        int portIndex = url.indexOf(URL_COLON, httpIndex);
        if (userPort == -1) {
            if (portIndex == -1 || portIndex > index) {
                this.mPort = mIsTSL ? 443 : 80;
            } else {
//                int endIndex = index == -1 ? url.length() : index;
                try {
                    this.mPort = Integer.parseInt(url.substring(portIndex + 1, index));
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

    public String getProtocol() {
        return mProtocol;
    }
}
