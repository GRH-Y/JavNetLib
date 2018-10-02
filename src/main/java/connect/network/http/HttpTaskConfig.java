package connect.network.http;

import connect.network.base.RequestEntity;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IHttpSSLFactory;
import connect.network.http.joggle.IHttpTaskConfig;
import connect.network.http.joggle.IRequestIntercept;
import connect.network.http.joggle.IResponseConvert;
import task.executor.ConsumerQueueAttribute;
import task.executor.interfaces.IConsumerAttribute;
import task.executor.interfaces.ITaskContainer;

public class HttpTaskConfig implements IHttpTaskConfig {

    private ITaskContainer mTaskContainer;
    private IHttpSSLFactory mSslFactory;
    private ISessionCallBack mSessionCallBack;
    private IConsumerAttribute<RequestEntity> mAttribute;
    private IResponseConvert mConvertResult;
    private IRequestIntercept mInterceptRequest;

    private long mFreeExitTime = 30000;
    private String mBaseUrl = null;

    public HttpTaskConfig() {
        mAttribute = new ConsumerQueueAttribute<>();
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
    public void setHttpSSLFactory(IHttpSSLFactory factory) {
        mSslFactory = factory;
    }

    @Override
    public void setFreeExit(long millisecond) {
        if (millisecond >= 0) {
            mFreeExitTime = millisecond;
        }
    }

    protected long getFreeExitTime() {
        return mFreeExitTime;
    }

    protected IHttpSSLFactory getSslFactory() {
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

    protected void onCallBackError(RequestEntity submitEntity) {
        if (mSessionCallBack != null && mTaskContainer.getTaskExecutor().getLoopState()) {
            submitEntity.setResultData(null);
            mSessionCallBack.notifyErrorMessage(submitEntity);
        }
    }

    protected void onCallBackSuccess(Object successData, RequestEntity submitEntity) {
        if (mSessionCallBack != null) {
            submitEntity.setResultData(successData);
            mSessionCallBack.notifySuccessMessage(submitEntity);
        }
    }


    /**
     * 释放资源
     */
    protected synchronized void recycle() {
        if (mTaskContainer != null) {
            mTaskContainer.getTaskExecutor().destroyTask();
        }
        if (mSessionCallBack != null) {
            mSessionCallBack.recycle();
            mSessionCallBack = null;
        }
        mAttribute = null;
    }

}
