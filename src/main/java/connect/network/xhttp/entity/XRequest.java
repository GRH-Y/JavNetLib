package connect.network.xhttp.entity;


import connect.network.base.RequestMode;
import connect.network.xhttp.utils.XUrlMedia;

import java.util.LinkedHashMap;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class XRequest {

    private XUrlMedia mHttpUrlMedia;
    private RequestMode mRequestMode = RequestMode.GET;

    //禁用系統的header参数，完全用户自定义
    private boolean mDisableSysProperty = false;

    private byte[] mSendData = null;

    private String mCallBackMethod = null;
    private String mProcessMethod = null;

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
    private LinkedHashMap<Object, Object> mRequestProperty = null;


    /**
     * 扩展参数
     */
    private Object exData;

    public XUrlMedia getUrl() {
        return mHttpUrlMedia;
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

    public String getCallBackMethod() {
        return mCallBackMethod;
    }

    public void setUrl(String url) {
        setUrl(url, -1);
    }

    public void setUrl(String url, int port) {
        if (mHttpUrlMedia == null) {
            mHttpUrlMedia = new XUrlMedia(url, port);
        } else {
            mHttpUrlMedia.reset(url, port);
        }
    }

    public void disableSysProperty() {
        this.mDisableSysProperty = true;
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


    public void setCallBackMethod(String callBackMethod) {
        this.mCallBackMethod = callBackMethod;
    }


    public String getProcessMethod() {
        return mProcessMethod;
    }

    public void setProcessMethod(String processMethod) {
        this.mProcessMethod = processMethod;
    }

    public void setResultType(Object resultType) {
        this.mResultType = resultType;
    }

    public void setObject(Object object) {
        this.exData = object;
    }

    public Object getObject() {
        return exData;
    }

    public void setRequestProperty(LinkedHashMap<Object, Object> property) {
        this.mRequestProperty = property;
    }

    public LinkedHashMap<Object, Object> getRequestProperty() {
        return mRequestProperty;
    }

    public boolean isDisableSysProperty() {
        return mDisableSysProperty;
    }
}
