package connect.network.xhttp.entity;

import connect.network.xhttp.XHttpProtocol;
import util.StringEnvoy;

public class XResponseHelper {
    private XResponseHelper() {
    }

    public static int getPort(XResponse response) {
        if (response == null) {
            return -1;
        }
        int port = 80;
        String content = response.getHeadForKey(XHttpProtocol.XY_FIST_LINE);
        if (StringEnvoy.isNotEmpty(content)) {
            String[] contentArrays = content.split(" ");
            if (contentArrays.length > 1) {
                String address = contentArrays[1];
                if (address.startsWith("https://")) {
                    port = 443;
                }
            }
        }
        String host = response.getHeadForKey(XHttpProtocol.XY_HOST);
        if (StringEnvoy.isNotEmpty(host)) {
            String[] hostArrays = host.split(":");
            if (hostArrays.length > 1) {
                try {
                    port = Integer.parseInt(hostArrays[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return port;
    }

    public static int getCode(XResponse response) {
        int code = 0;
        if (response == null) {
            return code;
        }
        String content = response.getHeadForKey(XHttpProtocol.XY_FIST_LINE);
        if (StringEnvoy.isNotEmpty(content) && content.startsWith("HTTP")) {
            String[] arrays = content.split(" ");
            if (arrays.length > 1) {
                try {
                    code = Integer.parseInt(arrays[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return code;
    }

    public static String getRequestMethod(XResponse response) {
        if (response == null) {
            return null;
        }
        String content = response.getHeadForKey(XHttpProtocol.XY_FIST_LINE);
        String[] contentArrays = content.split(" ");
        return contentArrays[0];
    }

    public static String getHost(XResponse response) {
        if (response == null) {
            return null;
        }
        String host = response.getHeadForKey(XHttpProtocol.XY_HOST)
                .replace("http://", "")
                .replace("https://", "")
                .split(":")[0];
        return host;
    }


    public static String getProtocol(XResponse response) {
        if (response == null) {
            return null;
        }
        String content = response.getHeadForKey(XHttpProtocol.XY_FIST_LINE);
        int httpIndex = content.indexOf("://");
        if (httpIndex > 0) {
            return content.substring(0, httpIndex);
        }
        return null;
    }

    public static boolean isTLS(XResponse response) {
        String protocol = getProtocol(response);
        return "https".equalsIgnoreCase(protocol);
    }

    public static String getVersion(XResponse response) {
        if (response == null) {
            return null;
        }
        String version = null;
        String content = response.getHeadForKey(XHttpProtocol.XY_FIST_LINE);
        String[] arrays = content.split(" ");
        if (arrays.length > 1) {
            version = arrays[0];
        }
        return version;
    }
}
