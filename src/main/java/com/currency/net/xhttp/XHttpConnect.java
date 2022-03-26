package com.currency.net.xhttp;

import com.currency.net.aio.AioClientFactory;
import com.currency.net.aio.AioClientTask;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.nio.NioClientFactory;
import com.currency.net.nio.NioClientTask;
import com.currency.net.xhttp.config.XHttpConfig;
import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.joggle.AXHttpRequest;
import com.currency.net.xhttp.joggle.IXHttpRequestEntity;

import java.util.LinkedHashMap;

public class XHttpConnect {

    private XHttpConfig mHttpConfig;

    private final XMultiplexCacheManger mHttpTaskManger;

    private final AbsNetFactory mNioNetFactory;

    private final AbsNetFactory mAioNetFactory;

    private volatile static XHttpConnect sHttpConnect = null;

    private final INetTaskContainer<AioClientTask> mAioNetTaskFactory;

    private final INetTaskContainer<NioClientTask> mNioNetTaskFactory;

    private XHttpConnect() {
        mHttpTaskManger = XMultiplexCacheManger.getInstance();
        mNioNetFactory = new NioClientFactory();
        mAioNetFactory = new AioClientFactory();
        mNioNetTaskFactory = mNioNetFactory.getNetTaskContainer();
        mAioNetTaskFactory = mAioNetFactory.getNetTaskContainer();
        mNioNetFactory.open();
        mAioNetFactory.open();
    }

    private static final class InnerClass {
        public static final XHttpConnect sConnect = new XHttpConnect();
    }

    public static XHttpConnect getInstance() {
        return InnerClass.sConnect;
    }

    public static void destroy() {
        synchronized (XHttpConnect.class) {
            if (sHttpConnect != null) {
                sHttpConnect.mNioNetFactory.close();
                sHttpConnect.mAioNetFactory.close();
                XMultiplexCacheManger.getInstance().destroy();
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

    private XRequest crateRequest(IXHttpRequestEntity entity, Object callBackTarget) {
        Class clx = entity.getClass();
        AXHttpRequest request = (AXHttpRequest) clx.getAnnotation(AXHttpRequest.class);
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

    public boolean submitRequest(IXHttpRequestEntity entity, Object callBackTarget) {
        if (entity == null) {
            throw new NullPointerException("entity is null ");
        }
        XRequest xHttpRequest = crateRequest(entity, callBackTarget);
        return submitRequest(xHttpRequest);
    }

    public boolean submitAioRequest(IXHttpRequestEntity entity, Object callBackTarget) {
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
        XNioHttpTask requestTask = mHttpTaskManger.obtainNioTask(mNioNetTaskFactory, mHttpConfig, request);
        return mNioNetTaskFactory.addExecTask(requestTask);
    }

    public boolean submitAioRequest(XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null ");
        }
        if (mHttpConfig == null) {
            mHttpConfig = XHttpConfig.getDefaultConfig();
        }
        XAioHttpTask requestTask = mHttpTaskManger.obtainAioTask(mAioNetTaskFactory, mHttpConfig, request);
        return mAioNetTaskFactory.addExecTask(requestTask);
    }
}