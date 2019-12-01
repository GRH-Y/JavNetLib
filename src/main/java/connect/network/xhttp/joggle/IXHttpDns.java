package connect.network.xhttp.joggle;

public interface IXHttpDns {

    void setCacheDns(String host, String ip);

    String findCacheDns(String host);

    void clearCache();
}
