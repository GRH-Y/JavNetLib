package connect.network.xhttp.config;

import connect.network.xhttp.joggle.IXHttpDns;
import util.StringEnvoy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class XHttpDefaultDns implements IXHttpDns {
    private Map<String, String> dnsMpa = new HashMap<>();

    @Override
    public void setCacheDns(String host, String ip) {
        synchronized (dnsMpa) {
            dnsMpa.put(host, ip);
        }
    }

    @Override
    public String findCacheDns(String host) {
        synchronized (dnsMpa) {
            String ip = dnsMpa.get(host);
            if (StringEnvoy.isEmpty(ip)) {
                try {
                    host = host.replace("http://", "").replace("https://", "");
                    InetAddress inetAddress = InetAddress.getByName(host);
                    ip = inetAddress.getHostAddress();
                    setCacheDns(host, ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            return ip;
        }
    }

    @Override
    public void clearCache() {
        synchronized (dnsMpa) {
            dnsMpa.clear();
        }
    }
}
