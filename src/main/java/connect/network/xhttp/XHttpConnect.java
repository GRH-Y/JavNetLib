package connect.network.xhttp;

import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.config.XHttpDefaultDns;
import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.joggle.IXHttpIntercept;

import java.util.Collection;

public class XHttpConnect {

    private XHttpConfig httpConfig = null;

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
            NioHPCClientFactory.getFactory(1).open();
            httpConfig = new XHttpConfig();
            XHttpDefaultDns dns = new XHttpDefaultDns();
            httpConfig.setXHttpDns(dns);
        }
        return httpConfig;
    }

    public XHttpConfig getHttpConfig() {
        return httpConfig;
    }

    public void submitRequest(XHttpRequest request) {
        XHttpRequestTask requestTask = httpTaskManger.obtain(request);
        IXHttpIntercept intercept = httpConfig.getIntercept();
        if (intercept != null) {
            boolean isIntercept = intercept.onStartRequestIntercept(request);
            if (isIntercept) {
                return;
            }
        }
        boolean ret = NioHPCClientFactory.getFactory().addTask(requestTask);
        if (ret) {
            httpTaskManger.pushTask(request.toString(), requestTask);
        }
    }

    public void cancelRequest(XHttpRequest request) {
        XHttpRequestTask targetRequest = httpTaskManger.getTask(request.toString());
        NioHPCClientFactory.getFactory().removeTask(targetRequest);
    }

    public void release() {
        Collection<XHttpRequestTask> collection = httpTaskManger.getAllTask();
        for (XHttpRequestTask task : collection) {
            NioHPCClientFactory.getFactory().removeTask(task);
        }
        httpTaskManger.release();
    }
}
