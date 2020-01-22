package connect.network.xhttp;

import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.config.XHttpDefaultDns;
import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.joggle.IXHttpIntercept;

import java.util.Collection;

public class XHttpConnect {

    private XHttpConfig httpConfig;

    private XHttpTaskManger httpTaskManger;

    private NioHPCClientFactory netFactory;

    private XHttpConnect() {
        httpConfig = new XHttpConfig();
        httpTaskManger = XHttpTaskManger.getInstance();
    }

    private static class HelperHolder {
        public static final XHttpConnect helper = new XHttpConnect();
    }

    public static XHttpConnect getInstance() {
        return HelperHolder.helper;
    }

    public XHttpConfig initDefault() {
        XHttpDefaultDns dns = new XHttpDefaultDns();
        httpConfig.setXHttpDns(dns);
        XHttpDefaultResponseConvert convert = new XHttpDefaultResponseConvert();
        httpConfig.setResponseConvert(convert);
        netFactory = new NioHPCClientFactory(2);
        netFactory.open();
        httpConfig.setNetFactory(netFactory);
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
        boolean ret = netFactory.addTask(requestTask);
        if (ret) {
            httpTaskManger.pushTask(request.toString(), requestTask);
        }
    }

    public void cancelRequest(XHttpRequest request) {
        XHttpRequestTask targetRequest = httpTaskManger.getTask(request.toString());
        netFactory.removeTask(targetRequest);
    }

    public void release() {
        Collection<XHttpRequestTask> collection = httpTaskManger.getAllTask();
        for (XHttpRequestTask task : collection) {
            netFactory.removeTask(task);
        }
        httpTaskManger.release();
    }
}
