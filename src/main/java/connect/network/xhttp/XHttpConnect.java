package connect.network.xhttp;

import connect.network.http.RequestEntity;
import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.config.XHttpDefaultDns;

public class XHttpConnect {

    private static XHttpConnect xHttpConnect;
    private XHttpConfig httpConfig;

    private XHttpConnect() {
    }

    public static XHttpConnect getInstance() {
        if (xHttpConnect == null) {
            synchronized (XHttpConnect.class) {
                if (xHttpConnect == null) {
                    xHttpConnect = new XHttpConnect();
                }
            }
        }
        return xHttpConnect;
    }

    public XHttpConfig init() {
        if (httpConfig == null) {
            NioHPCClientFactory.getFactory().open();
            httpConfig = new XHttpConfig();
            XHttpDefaultDns dns = new XHttpDefaultDns();
            httpConfig.setXHttpDns(dns);
        }
        return httpConfig;
    }

    public XHttpConfig getHttpConfig() {
        return httpConfig;
    }

    public void submitRequest(RequestEntity entity) {
        XHttpRequestTask requestTask = new XHttpRequestTask(entity);
        NioHPCClientFactory.getFactory().addTask(requestTask);
    }
}
