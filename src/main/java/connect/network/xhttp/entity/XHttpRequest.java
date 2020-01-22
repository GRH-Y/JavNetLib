package connect.network.xhttp.entity;


import connect.network.base.RequestMode;

import java.util.Map;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class XHttpRequest {

    private boolean isDisableBaseUrl = false;

    private String address = null;
    private String referer = null;
    private String host = null;
    private String path = null;
    private int port = 0;
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

    public boolean isDisableBaseUrl() {
        return isDisableBaseUrl;
    }

    public void setDisableBaseUrl(boolean enableBaseUrl) {
        isDisableBaseUrl = enableBaseUrl;
    }

    public String getAddress() {
        return address;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getReferer() {
        return referer;
    }

    public int getPort() {
        return port;
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


    public void setAddress(String address) {
        this.address = address;
        int httpIndex = address.indexOf("://") + 3;
        int index = address.indexOf("/", httpIndex);
        if (index > 0) {
            path = address.substring(index);
        } else {
            index = address.length();
        }
        host = address.substring(httpIndex, index);
        referer = address.substring(0, index) + "/";
        if (port == 0) {
            if (address.startsWith("https")) {
                port = 443;
            } else {
                port = 80;
            }
        }
    }

    public void setPort(int port) {
        this.port = port;
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
