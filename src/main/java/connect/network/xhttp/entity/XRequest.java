package connect.network.xhttp.entity;


import connect.network.base.RequestMode;
import connect.network.xhttp.utils.XUrlMedia;

import java.util.Map;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class XRequest {

    private XUrlMedia httpUrlMedia;
    private RequestMode requestMode = RequestMode.GET;

    private byte[] sendData = null;

    private String successMethod = null;
    private String errorMethod = null;
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
    private Map<String, Object> requestProperty = null;


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

    public String getSuccessMethod() {
        return successMethod;
    }

    public String getErrorMethod() {
        return errorMethod;
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

    public void setRequestMode(RequestMode requestMode) {
        if (requestMode == null) {
            throw new NullPointerException("requestMode is null !!!");
        }
        this.requestMode = requestMode;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }


    public void setSuccessMethod(String successMethod) {
        this.successMethod = successMethod;
    }


    public String getProcessMethod() {
        return processMethod;
    }

    public void setErrorMethod(String errorMethod) {
        this.errorMethod = errorMethod;
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

    public void setRequestProperty(Map<String, Object> property) {
        this.requestProperty = property;
    }

    public Map<String, Object> getRequestProperty() {
        return requestProperty;
    }

}
