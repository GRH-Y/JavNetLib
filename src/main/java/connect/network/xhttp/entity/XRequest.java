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

    private XUrlMedia httpUrlMedia;
    private RequestMode requestMode = RequestMode.GET;

    //禁用系統的header参数，完全用户自定义
    private boolean disableSysProperty = false;

    private byte[] sendData = null;

    private String callBackMethod = null;
    private String processMethod = null;

    /**
     * 回调接口返回结果的值
     */
    private Object resultType = null;
    /**
     * 接口回调类
     */
    private Object callBackTarget = null;

    /**
     * 请求头参数
     */
    private LinkedHashMap<Object, Object> requestProperty = null;


    /**
     * 扩展参数
     */
    private Object object;

    public XUrlMedia getUrl() {
        return httpUrlMedia;
    }

    public RequestMode getRequestMode() {
        return requestMode;
    }

    public byte[] getSendData() {
        return sendData;
    }

    public Object getCallBackTarget() {
        return callBackTarget;
    }

    public void setCallBackTarget(Object callBackTarget) {
        this.callBackTarget = callBackTarget;
    }

    public Object getResultType() {
        return resultType;
    }

    public String getCallBackMethod() {
        return callBackMethod;
    }

    public void setUrl(String url) {
        setUrl(url, -1);
    }

    public void setUrl(String url, int port) {
        if (httpUrlMedia == null) {
            httpUrlMedia = new XUrlMedia(url, port);
        } else {
            httpUrlMedia.reset(url, port);
        }
    }

    public void disableSysProperty() {
        this.disableSysProperty = true;
    }

    public void setRequestMode(RequestMode requestMode) {
        if (requestMode == null) {
            throw new NullPointerException("requestMode is null !!!");
        }
        this.requestMode = requestMode;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }


    public void setCallBackMethod(String callBackMethod) {
        this.callBackMethod = callBackMethod;
    }


    public String getProcessMethod() {
        return processMethod;
    }

    public void setProcessMethod(String processMethod) {
        this.processMethod = processMethod;
    }

    public void setResultType(Object resultType) {
        this.resultType = resultType;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setRequestProperty(LinkedHashMap<Object, Object> property) {
        this.requestProperty = property;
    }

    public LinkedHashMap<Object, Object> getRequestProperty() {
        return requestProperty;
    }

    public boolean isDisableSysProperty() {
        return disableSysProperty;
    }
}
