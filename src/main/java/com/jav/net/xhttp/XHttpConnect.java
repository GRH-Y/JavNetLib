package com.jav.net.xhttp;

import com.jav.net.aio.AioClientFactory;
import com.jav.net.aio.AioClientTask;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioClientFactory;
import com.jav.net.nio.NioClientTask;
import com.jav.net.xhttp.config.XHttpConfig;
import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.joggle.AXHttpRequest;
import com.jav.net.xhttp.joggle.IXHttpRequestEntity;

import java.util.LinkedHashMap;

public class XHttpConnect {

    private XHttpConfig mHttpConfig;

    private final XMultiplexCacheManger mHttpTaskManger;

    private final AbsNetFactory mNioNetFactory;

    private final AbsNetFactory mAioNetFactory;

    private volatile static XHttpConnect sHttpConnect = null;

    private final INetTaskComponent<AioClientTask> mAioNetTaskFactory;

    private final INetTaskComponent<NioClientTask> mNioNetTaskFactory;

    private XHttpConnect() {
        mHttpTaskManger = XMultiplexCacheManger.getInstance();
        mNioNetFactory = new NioClientFactory();
        mAioNetFactory = new AioClientFactory();
        mNioNetFactory.open();
        mAioNetFactory.open();

        mNioNetTaskFactory = mNioNetFactory.getNetTaskComponent();
        mAioNetTaskFactory = mAioNetFactory.getNetTaskComponent();
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
        Class<?> clx = entity.getClass();
        AXHttpRequest request = clx.getAnnotation(AXHttpRequest.class);
        if (request == null) {
            throw new IllegalArgumentException("The entity has no annotations ARequest !!! ");
        }
        XRequest xHttpRequest = new XRequest();

        LinkedHashMap<Object, Object> property = entity.getRequestProperty();
        xHttpRequest.setRequestProperty(property);
        xHttpRequest.setCallBackTarget(callBackTarget);
        xHttpRequest.setSendData(entity.getSendData());
        xHttpRequest.setCallBackSuccessMethod(request.callBackSuccessMethod());
        xHttpRequest.setCallBackErrorMethod(request.callBackErrorMethod());
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


    public boolean submitNioRequest(IXHttpRequestEntity entity, Object callBackTarget) {
        if (entity == null) {
            throw new NullPointerException("entity is null ");
        }
        XRequest xHttpRequest = crateRequest(entity, callBackTarget);
        return submitNioRequest(xHttpRequest);
    }

    public boolean submitNioRequest(XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null ");
        }
        if (mHttpConfig == null) {
            mHttpConfig = XHttpConfig.getDefaultConfig();
        }
        XNioHttpTask requestTask = mHttpTaskManger.obtainNioTask(mNioNetTaskFactory, mHttpConfig, request);
        return mNioNetTaskFactory.addExecTask(requestTask);
    }

    public boolean submitAioRequest(IXHttpRequestEntity entity, Object callBackTarget) {
        if (entity == null) {
            throw new NullPointerException("entity is null ");
        }
        XRequest xHttpRequest = crateRequest(entity, callBackTarget);
        return submitAioRequest(xHttpRequest);
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
