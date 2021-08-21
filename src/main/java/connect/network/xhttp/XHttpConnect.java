package connect.network.xhttp;

import connect.network.aio.AioClientFactory;
import connect.network.base.AbsNetFactory;
import connect.network.http.joggle.AXRequest;
import connect.network.http.joggle.IRequestEntity;
import connect.network.nio.NioClientFactory;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;

import java.util.LinkedHashMap;

public class XHttpConnect {

    private XHttpConfig mHttpConfig;

    private XMultiplexCacheManger mHttpTaskManger;

    private AbsNetFactory mNioNetFactory;

    private AbsNetFactory mAioNetFactory;

    private static XHttpConnect sHttpConnect = null;

    private XHttpConnect() {
        mHttpTaskManger = XMultiplexCacheManger.getInstance();
        mNioNetFactory = new NioClientFactory();
        mAioNetFactory = new AioClientFactory();
        mNioNetFactory.open();
        mAioNetFactory.open();
    }

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
                sHttpConnect.mNioNetFactory.close();
                sHttpConnect.mAioNetFactory.close();
                XMultiplexCacheManger.destroy();
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

    private XRequest crateRequest(IRequestEntity entity, Object callBackTarget) {
        Class clx = entity.getClass();
        AXRequest request = (AXRequest) clx.getAnnotation(AXRequest.class);
        if (request == null) {
            throw new IllegalArgumentException("The entity has no annotations ARequest !!! ");
        }
        XRequest xHttpRequest = new XRequest();

        LinkedHashMap<Object, Object> property = entity.getRequestProperty();
        xHttpRequest.setRequestProperty(property);
        xHttpRequest.setCallBackTarget(callBackTarget);
        xHttpRequest.setSendData(entity.getSendData());
        xHttpRequest.setCallBackMethod(request.callBackMethod());
        xHttpRequest.setProcessMethod(request.processMethod());
        xHttpRequest.setResultType(request.resultType());
        xHttpRequest.setRequestMode(request.requestMode());
        xHttpRequest.setUrl(request.url());
        boolean isDisableSysProperty = request.disableSysProperty();
        if (isDisableSysProperty) {
            xHttpRequest.disableSysProperty();
        }
        return xHttpRequest;
    }

    public boolean submitRequest(IRequestEntity entity, Object callBackTarget) {
        if (entity == null) {
            throw new NullPointerException("entity is null ");
        }
        XRequest xHttpRequest = crateRequest(entity, callBackTarget);
        return submitRequest(xHttpRequest);
    }

    public boolean submitAioRequest(IRequestEntity entity, Object callBackTarget) {
        if (entity == null) {
            throw new NullPointerException("entity is null ");
        }
        XRequest xHttpRequest = crateRequest(entity, callBackTarget);
        return submitAioRequest(xHttpRequest);
    }

    public boolean submitRequest(XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null ");
        }
        if (mHttpConfig == null) {
            mHttpConfig = XHttpConfig.getDefaultConfig();
        }
        XNioHttpTask requestTask = mHttpTaskManger.obtainNioTask(mNioNetFactory, mHttpConfig, request);
        return mNioNetFactory.addTask(requestTask);
    }

    public boolean submitAioRequest(XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null ");
        }
        if (mHttpConfig == null) {
            mHttpConfig = XHttpConfig.getDefaultConfig();
        }
        XAioHttpTask requestTask = mHttpTaskManger.obtainAioTask(mAioNetFactory, mHttpConfig, request);
        return mAioNetFactory.addTask(requestTask);
    }
}
