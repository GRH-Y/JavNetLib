package connect.network.base;

import connect.network.http.JavHttpConnect;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class RequestEntity {
    /**
     * 任务的tag，区分不同类型请求（用于取消同个tag的任务请求）
     */
    private int taskTag = JavHttpConnect.DEFAULT_TASK_TAG;
    private String address = null;
    private String requestMethod = null;

    private byte[] sendData = null;
    private Object resultData = null;

    private String scbMethodName = null;
    private String ecbMethodName = null;
    private boolean isAutoSetDataForView = false;
    /**
     * 如果true则调用成功回调接口
     */
    private boolean isResultStatus = true;
    /**
     * 接口回调类
     */
    private Object callBackTarget = null;
    /**
     * 自动为控件设置值的类（一般是 activity fragment view windows）
     */
    private Object viewTarget = null;

    public int getTaskTag() {
        return taskTag;
    }

    public void setTaskTag(int taskTag) {
        this.taskTag = taskTag;
    }

    /**
     * 回调接口返回结果的值
     */
    private Object resultType = null;

    public String getAddress() {
        return address;
    }

    public String getRequestMethod() {
        return requestMethod;
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

    public boolean isAutoSetDataForView() {
        return isAutoSetDataForView;
    }

    public String getSuccessMethodName() {
        return scbMethodName;
    }

    public String getErrorMethodName() {
        return ecbMethodName;
    }

    public Object getResultData() {
        return resultData;
    }

    public void setResultData(Object resultData) {
        this.resultData = resultData;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }

    public String getScbMethodName() {
        return scbMethodName;
    }

    public void setScbMethodName(String scbMethodName) {
        this.scbMethodName = scbMethodName;
    }

    public String getEcbMethodName() {
        return ecbMethodName;
    }

    public void setEcbMethodName(String ecbMethodName) {
        this.ecbMethodName = ecbMethodName;
    }

    public void setAutoSetDataForView(boolean autoSetDataForView) {
        isAutoSetDataForView = autoSetDataForView;
    }

    public void setResultType(Object resultType) {
        this.resultType = resultType;
    }

    public boolean isResultStatus() {
        return isResultStatus;
    }

    public void setResultStatus(boolean resultStatus) {
        isResultStatus = resultStatus;
    }

    public Object getViewTarget() {
        return viewTarget;
    }

    public void setViewTarget(Object viewTarget) {
        this.viewTarget = viewTarget;
    }
}
