package connect.network.xhttp.joggle;

public interface IXHttpDns {

    void setCacheDns(String host, String ip);

    String getCacheDns(String host);

    String findCacheDns(String host);

    void clearCache();
}
