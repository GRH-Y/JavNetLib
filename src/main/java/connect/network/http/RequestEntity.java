package connect.network.http;


import java.util.Map;

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
    private String requestMethod = ConnectType.GET.getType();

    private byte[] sendData = null;
    private byte[] respond = null;
    private Object respondEntity = null;

    private String scbMethodName = null;
    private String ecbMethodName = null;
    private String processMethod = null;
    private boolean isAutoSetDataForView = false;
    private boolean isDisableBaseUrl = false;

    /**
     * 回调接口返回结果的值
     */
    private Object resultType = null;
    /**
     * 接口回调类
     */
    private Object callBackTarget = null;
    /**
     * 自动为控件设置值的类（一般是 activity fragment view windows）
     */
    private Object viewTarget = null;

    private Map<String, Object> requestProperty = null;

    /**
     * 是否独立任务（如果是独立任务会单独开启线程处理）
     */
    private boolean isIndependentTask = false;

    public boolean isDisableBaseUrl() {
        return isDisableBaseUrl;
    }

    public void setDisableBaseUrl(boolean enableBaseUrl) {
        isDisableBaseUrl = enableBaseUrl;
    }

    public int getTaskTag() {
        return taskTag;
    }

    public void setTaskTag(int taskTag) {
        this.taskTag = taskTag;
    }

    public void setRequestProperty(Map<String, Object> property) {
        this.requestProperty = property;
    }

    public Map<String, Object> getRequestProperty() {
        return requestProperty;
    }

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

    public String getSuccessMethodName() {
        return scbMethodName;
    }

    public String getErrorMethodName() {
        return ecbMethodName;
    }

    public Object getRespondEntity() {
        return respondEntity;
    }

    protected void setRespondEntity(Object respondEntity) {
        this.respondEntity = respondEntity;
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

    public String getProcessMethodName() {
        return processMethod;
    }

    public void setEcbMethodName(String ecbMethodName) {
        this.ecbMethodName = ecbMethodName;
    }

    public void setProcessMethodName(String processMethod) {
        this.processMethod = processMethod;
    }

    public void setAutoSetDataForView(boolean autoSetDataForView) {
        isAutoSetDataForView = autoSetDataForView;
    }

    public boolean isAutoSetDataForView() {
        return isAutoSetDataForView;
    }

    public void setResultType(Object resultType) {
        this.resultType = resultType;
    }

    public Object getViewTarget() {
        return viewTarget;
    }

    public void setViewTarget(Object viewTarget) {
        this.viewTarget = viewTarget;
    }

    public void setIndependentTask(boolean independentTask) {
        isIndependentTask = independentTask;
    }

    public boolean isIndependentTask() {
        return isIndependentTask;
    }

    public void setRespond(byte[] respond) {
        this.respond = respond;
    }

    public byte[] getRespond() {
        return respond;
    }
}
