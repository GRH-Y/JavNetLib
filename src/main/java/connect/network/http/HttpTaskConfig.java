package connect.network.http;

import connect.json.JsonUtils;
import connect.network.base.RequestEntity;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IConvertResult;
import connect.network.http.joggle.IHttpTaskConfig;
import connect.network.http.joggle.IInterceptRequest;
import task.executor.interfaces.IConsumerAttribute;
import task.executor.interfaces.ILoopTaskExecutor;

public class HttpTaskConfig implements IHttpTaskConfig {

    private ILoopTaskExecutor mExecutor;
    private ISessionCallBack mSessionCallBack;
    private IConsumerAttribute<RequestEntity> mAttribute;
    private IConvertResult mConvertResult;
    private IInterceptRequest mInterceptRequest;

    private long mFreeExitTime = 30000;
    private String mBaseUrl = null;

    protected void setAttribute(IConsumerAttribute<RequestEntity> attribute) {
        this.mAttribute = attribute;
    }

    protected void setExecutor(ILoopTaskExecutor executor) {
        this.mExecutor = executor;
    }

    protected IConsumerAttribute<RequestEntity> getAttribute() {
        return mAttribute;
    }

    protected ILoopTaskExecutor getExecutor() {
        return mExecutor;
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
    public void setFreeExit(long millisecond) {
        if (millisecond >= 0) {
            mFreeExitTime = millisecond;
        }
    }

    protected void onCheckIsIdle() {
        if (mAttribute.getCacheDataSize() == 0) {
            mExecutor.waitTask(mFreeExitTime);
            if (mAttribute.getCacheDataSize() == 0) {
                mExecutor.stopTask();
            }
        }
    }

    protected RequestEntity popCacheData() {
        return mAttribute.popCacheData();
    }

    protected void onCallBackError(RequestEntity submitEntity) {
        if (mSessionCallBack != null && mExecutor.getLoopState()) {
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
        if (mExecutor != null) {
            mExecutor.destroyTask();
            mExecutor = null;
        }
        if (mSessionCallBack != null) {
            mSessionCallBack.recycle();
            mSessionCallBack = null;
        }
        mAttribute = null;
    }

}
