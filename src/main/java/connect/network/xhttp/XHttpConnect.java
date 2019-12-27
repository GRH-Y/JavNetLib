package connect.network.xhttp;

import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.config.XHttpDefaultDns;
import connect.network.xhttp.entity.XHttpRequest;

public class XHttpConnect {

    private XHttpConfig httpConfig;

    public static long starTime = 0;

    private XHttpTaskManger httpTaskManger;

    private XHttpConnect() {
        httpTaskManger = XHttpTaskManger.getInstance();
    }

    private static class HelperHolder {
        public static final XHttpConnect helper = new XHttpConnect();
    }

    public static XHttpConnect getInstance() {
        return HelperHolder.helper;
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
        starTime = System.currentTimeMillis();
        XHttpRequestTask requestTask = new XHttpRequestTask(entity);//7ms
        boolean ret = NioHPCClientFactory.getFactory().addTask(requestTask);
        if (ret) {
            httpTaskManger.pushTask(entity.toString(), requestTask);
        }
    }

    public void closeRequest(XHttpRequest entity) {
        XHttpRequestTask requestTask = httpTaskManger.removerTask(entity.toString());
        NioHPCClientFactory.getFactory().removeTask(requestTask);
    }
}
