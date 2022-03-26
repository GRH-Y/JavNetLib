package com.currency.net.xhttp.entity;


import com.currency.net.entity.RequestMode;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class XHttpRequestEntity {

    /**
     * 默认的请求tag
     */
    public static final int DEFAULT_TASK_TAG = 0;
    /**
     * 任务的tag，区分不同类型请求（用于取消同个tag的任务请求）
     */
    private int mTaskTag = DEFAULT_TASK_TAG;
    private String mAddress = null;
    private int mPort = 80;
    private RequestMode mRequestMode = RequestMode.GET;

    private int mResponseCode = HttpURLConnection.HTTP_OK;
    private byte[] mSendData = null;
    private byte[] mRespondData = null;
    private Object mRespondEntity = null;

    private String mSuccessMethod = null;
    private String mErrorMethod = null;
    private String mProcessMethod = null;

    private boolean mIsDisableBaseUrl = false;

    /**
     * 回调接口返回结果的值
     */
    private Object mResultType = null;
    /**
     * 接口回调类
     */
    private Object mCallBackTarget = null;

    /**
     * 请求头参数
     */
    private Map<Object, Object> mRequestProperty = null;

    /**
     * 响应头参数
     */
    private Map<String, List<String>> mResponseProperty = null;

    /**
     * 是否独立任务（如果是独立任务会单独开启线程处理）
     */
    private boolean mIsIndependentTask = false;

    /**
     * 扩展参数
     */
    private Object mExData;

    private Throwable mException;

    public boolean isDisableBaseUrl() {
        return mIsDisableBaseUrl;
    }

    public void setDisableBaseUrl(boolean enableBaseUrl) {
        mIsDisableBaseUrl = enableBaseUrl;
    }

    public int getTaskTag() {
        return mTaskTag;
    }

    public void setTaskTag(int taskTag) {
        this.mTaskTag = taskTag;
    }

    public void setRequestProperty(Map<Object, Object> property) {
        this.mRequestProperty = property;
    }

    public Map<Object, Object> getRequestProperty() {
        return mRequestProperty;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getPort() {
        return mPort;
    }

    public RequestMode getRequestMode() {
        return mRequestMode;
    }

    public byte[] getSendData() {
        return mSendData;
    }

    public Object getCallBackTarget() {
        return mCallBackTarget;
    }

    public void setCallBackTarget(Object callBackTarget) {
        this.mCallBackTarget = callBackTarget;
    }

    public Object getResultType() {
        return mResultType;
    }

    public String getSuccessMethod() {
        return mSuccessMethod;
    }

    public String getErrorMethod() {
        return mErrorMethod;
    }

    public <T> T getRespondEntity() {
        return (T) mRespondEntity;
    }

    public void setRespondEntity(Object respondEntity) {
        this.mRespondEntity = respondEntity;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public void setRequestMode(RequestMode requestMode) {
        if (requestMode == null) {
            throw new NullPointerException("requestMode is null !!!");
        }
        this.mRequestMode = requestMode;
    }

    public void setSendData(byte[] sendData) {
        this.mSendData = sendData;
    }


    public void setSuccessMethod(String successMethod) {
        this.mSuccessMethod = successMethod;
    }


    public String getProcessMethod() {
        return mProcessMethod;
    }

    public void setErrorMethod(String errorMethod) {
        this.mErrorMethod = errorMethod;
    }

    public void setProcessMethod(String processMethod) {
        this.mProcessMethod = processMethod;
    }

    public void setResultType(Object resultType) {
        this.mResultType = resultType;
    }

    public void setIndependentTask(boolean independentTask) {
        mIsIndependentTask = independentTask;
    }

    public boolean isIndependentTask() {
        return mIsIndependentTask;
    }

    public void setRespondData(byte[] respondData) {
        this.mRespondData = respondData;
    }

    public byte[] getRespondData() {
        return mRespondData;
    }

    protected void setResponseCode(int responseCode) {
        this.mResponseCode = responseCode;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public void setExData(Object exData) {
        this.mExData = exData;
    }

    public Object getExData() {
        return mExData;
    }

    protected void setResponseProperty(Map<String, List<String>> responseProperty) {
        this.mResponseProperty = responseProperty;
    }

    public void setException(Throwable e) {
        this.mException = e;
    }

    public Map<String, List<String>> getResponseProperty() {
        return mResponseProperty;
    }

    public Throwable getException() {
        return mException;
    }
}
