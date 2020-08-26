package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.nio.NioClientFactory;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.joggle.IXHttpIntercept;

import java.util.Collection;

public class XHttpConnect {

    private XHttpConfig mHttpConfig;

    private XHttpRequestTaskManger mHttpTaskManger;

    private AbsNetFactory mNetFactory;

    private XHttpConnect() {
        mHttpTaskManger = XHttpRequestTaskManger.getInstance();
        mNetFactory = new NioClientFactory();
        mNetFactory.open();
    }

    private static XHttpConnect sHttpConnect = null;

    public static synchronized XHttpConnect getInstance() {
        if (sHttpConnect == null) {
            synchronized (XHttpConnect.class) {
                if (sHttpConnect == null) {
                    sHttpConnect = new XHttpConnect();
                }
            }
        }
        return sHttpConnect;
    }

    public static void destroy() {
        synchronized (XHttpConnect.class) {
            if (sHttpConnect != null) {
                sHttpConnect.mNetFactory.close();
                XHttpRequestTaskManger.destroy();
                sHttpConnect = null;
            }
        }
    }

    public void setHttpConfig(XHttpConfig httpConfig) {
        if (httpConfig == null) {
            httpConfig = XHttpConfig.getDefaultConfig();
        }
        this.mHttpConfig = httpConfig;
    }

    public XHttpConfig getHttpConfig() {
        return mHttpConfig;
    }

    protected AbsNetFactory getNetFactory() {
        return mNetFactory;
    }

    public boolean submitRequest(XRequest request) {
        if (request == null) {
            return false;
        }
        if (mHttpConfig == null) {
            mHttpConfig = XHttpConfig.getDefaultConfig();
        }
        IXHttpIntercept intercept = mHttpConfig.getIntercept();
        if (intercept != null) {
            boolean isIntercept = intercept.onStartRequestIntercept(request);
            if (isIntercept) {
                return false;
            }
        }
        XHttpRequestTask requestTask = mHttpTaskManger.obtain(mNetFactory, mHttpConfig, request);
        boolean ret = mNetFactory.addTask(requestTask);
        if (ret) {
            mHttpTaskManger.pushTask(request.toString(), requestTask);
        }
        return ret;
    }

    public void cancelRequest(XRequest request) {
        if (request == null) {
            return;
        }
        XHttpRequestTask targetRequest = mHttpTaskManger.getTask(request.toString());
        mNetFactory.removeTask(targetRequest);
    }

    public void cancelAllRequest() {
        Collection<XHttpRequestTask> collection = mHttpTaskManger.getAllTask();
        for (XHttpRequestTask task : collection) {
            mNetFactory.removeTask(task);
        }
    }
}
