package connect.network.xhttp.utils;

import log.LogDog;
import util.StringEnvoy;

public class XUrlMedia {

    private String url;
    private String protocol;
    private String referer = null;
    private String host = null;
    private String path = "/";
    private int port = 0;
    private boolean isTSL = false;

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
        this.protocol = url.substring(0, httpIndex);
        this.isTSL = "https".equalsIgnoreCase(this.protocol);
        httpIndex += 3;
        int index = url.indexOf("/", httpIndex);
        if (index > 0) {
            this.path = url.substring(index);
        } else {
            index = url.length();
        }

        int portIndex = url.indexOf(":", httpIndex);
        if (userPort == -1) {
            if (portIndex == -1 || portIndex > index) {
                this.port = isTSL ? 443 : 80;
            } else {
                int endIndex = index == -1 ? url.length() : index;
                try {
                    this.port = Integer.parseInt(url.substring(portIndex + 1, endIndex));
                } catch (Exception e) {
                    LogDog.e(" url = " + url);
                    e.printStackTrace();
                }
            }
        } else {
            this.port = userPort;
        }
        this.host = url.substring(httpIndex, portIndex == -1 || portIndex > index ? index : portIndex);
        this.referer = url.substring(0, index) + "/";
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getReferer() {
        return referer;
    }

    public int getPort() {
        return port;
    }

    public boolean isTSL() {
        return isTSL;
    }
}
