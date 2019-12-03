package connect.network.xhttp;

import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.config.XHttpDefaultDns;
import connect.network.xhttp.entity.XHttpRequest;

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

    public void submitRequest(XHttpRequest entity) {
        XHttpRequestTask requestTask = new XHttpRequestTask(entity);
        NioHPCClientFactory.getFactory().addTask(requestTask);
    }
}
