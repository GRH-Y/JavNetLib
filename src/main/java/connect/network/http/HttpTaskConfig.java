package connect.network.http;

import connect.json.JsonUtils;
import connect.network.base.RequestEntity;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IConvertResult;
import connect.network.http.joggle.IHttpTaskConfig;
import connect.network.http.joggle.IInterceptRequest;
import connect.network.http.joggle.IHttpSSLFactory;
import task.executor.ConsumerQueueAttribute;
import task.executor.interfaces.IConsumerAttribute;
import task.executor.interfaces.ITaskContainer;

public class HttpTaskConfig implements IHttpTaskConfig {

    private ITaskContainer mTaskContainer;
    private IHttpSSLFactory mSslFactory;
    private ISessionCallBack mSessionCallBack;
    private IConsumerAttribute<RequestEntity> mAttribute;
    private IConvertResult mConvertResult;
    private IInterceptRequest mInterceptRequest;

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
    public void setConvertResult(IConvertResult convertResult) {
        this.mConvertResult = convertResult;
    }

    @Override
    public void setInterceptRequest(IInterceptRequest interceptRequest) {
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

    protected void onCheckIsIdle() {
        if (mAttribute.getCacheDataSize() == 0) {
            mTaskContainer.getTaskExecutor().waitTask(mFreeExitTime);
            if (mAttribute.getCacheDataSize() == 0) {
                mTaskContainer.getTaskExecutor().stopTask();
            }
        }
    }

    protected IHttpSSLFactory getSslFactory() {
        return mSslFactory;
    }

    protected RequestEntity popCacheData() {
        return mAttribute.popCacheData();
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

    protected Object onConvertResult(Class resultCls, String result) {
        if (mConvertResult != null) {
            return mConvertResult.handlerEntity(resultCls, result);
        } else {
            return JsonUtils.toEntity(resultCls, result);
        }
    }

    /**
     * 拦截请求(已请求返回，但没有处理请求返回结果)
     *
     * @return 返回true则需要拦截
     */
    protected boolean intercept(RequestEntity submitEntity) {
        if (mInterceptRequest != null) {
            return mInterceptRequest.intercept(submitEntity);
        }
        return false;
    }

    /**
     * 拦截请求回调结果(已处理返回结果，但没有回调返回结果)
     *
     * @return 返回true则需要拦截
     */
    protected boolean interceptCallBack(RequestEntity submitEntity, Object entity) {
        if (mInterceptRequest != null) {
            return mInterceptRequest.interceptCallBack(submitEntity, entity);
        }
        return false;
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
