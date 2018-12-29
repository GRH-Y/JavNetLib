package connect.network.http;


import connect.network.base.RequestEntity;
import connect.network.base.joggle.ISSLFactory;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IHttpTaskConfig;
import connect.network.http.joggle.IRequestIntercept;
import connect.network.http.joggle.IResponseConvert;
import task.executor.ConsumerQueueAttribute;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ITaskContainer;

import java.util.Map;

public class HttpTaskConfig implements IHttpTaskConfig {

    public static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static String CONTENT_TYPE_MULTI = "multipart/form-data";
    public static String CONTENT_TYPE_JSON = "application/json";
    public static String CONTENT_TYPE_XML = "text/xml";

    private ITaskContainer mTaskContainer;
    private ISSLFactory mSslFactory;
    private ISessionCallBack mSessionCallBack;
    private IConsumerAttribute<RequestEntity> mAttribute;
    private IResponseConvert mConvertResult;
    private IRequestIntercept mInterceptRequest;

    private long mFreeExitTime = 30000;
    private int mTimeout = 8000;
    private String mBaseUrl = null;
    private Map<String, String> mRequestProperty = null;

    public HttpTaskConfig() {
        mAttribute = new ConsumerQueueAttribute<>();
    }


    @Override
    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    @Override
    public void setGlobalRequestProperty(Map<String, String> property) {
        this.mRequestProperty = property;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Override
    public void setSessionCallBack(ISessionCallBack sessionCallBack) {
        this.mSessionCallBack = sessionCallBack;
    }

    @Override
    public void setConvertResult(IResponseConvert convertResult) {
        this.mConvertResult = convertResult;
    }

    @Override
    public void setInterceptRequest(IRequestIntercept interceptRequest) {
        this.mInterceptRequest = interceptRequest;
    }

    @Override
    public void setHttpSSLFactory(ISSLFactory factory) {
        mSslFactory = factory;
    }

    @Override
    public void setFreeExit(long millisecond) {
        if (millisecond >= 0) {
            mFreeExitTime = millisecond;
        }
    }

    protected void setTaskContainer(ITaskContainer container) {
        this.mTaskContainer = container;
    }

    protected IConsumerAttribute<RequestEntity> getAttribute() {
        return mAttribute;
    }

    protected <T> T getExecutor() {
        return mTaskContainer.getTaskExecutor();
    }

    protected String getBaseUrl() {
        return mBaseUrl;
    }

    protected long getFreeExitTime() {
        return mFreeExitTime;
    }

    protected ISSLFactory getSslFactory() {
        return mSslFactory;
    }

    protected RequestEntity popCacheData() {
        return mAttribute.popCacheData();
    }

    protected IRequestIntercept getInterceptRequest() {
        return mInterceptRequest;
    }

    protected IResponseConvert getConvertResult() {
        return mConvertResult;
    }

    protected ISessionCallBack getSessionCallBack() {
        return mSessionCallBack;
    }

    protected int getTimeout() {
        return mTimeout;
    }

    protected Map<String, String> getGlobalRequestProperty() {
        return mRequestProperty;
    }

    /**
     * 释放资源
     */
    protected synchronized void recycle() {
        if (mTaskContainer != null) {
            mTaskContainer.getTaskExecutor().destroyTask();
        }
        mAttribute.clearCacheData();
        mAttribute = null;
    }

}
