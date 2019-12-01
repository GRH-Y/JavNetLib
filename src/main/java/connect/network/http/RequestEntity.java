package connect.network.http;


import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * 请求网络任务实体
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */
public class RequestEntity {

    /**
     * 默认的请求tag
     */
    public static final int DEFAULT_TASK_TAG = 0;
    /**
     * 任务的tag，区分不同类型请求（用于取消同个tag的任务请求）
     */
    private int taskTag = DEFAULT_TASK_TAG;
    private String address = null;
    private int port = 0;
    private RequestMode requestMode = RequestMode.GET;

    private int responseCode = HttpURLConnection.HTTP_OK;
    private byte[] sendData = null;
    private byte[] respondData = null;
    private Object respondEntity = null;

    private String successMethod = null;
    private String errorMethod = null;
    private String processMethod = null;

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
     * 请求头参数
     */
    private Map<String, Object> requestProperty = null;

    /**
     * 响应头参数
     */
    private Map<String, List<String>> responseProperty = null;

    /**
     * 是否独立任务（如果是独立任务会单独开启线程处理）
     */
    private boolean isIndependentTask = false;

    /**
     * 扩展参数
     */
    private Object object;

    private Throwable exception;

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

    public Object getRespondEntity() {
        return respondEntity;
    }

    public void setRespondEntity(Object respondEntity) {
        this.respondEntity = respondEntity;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRequestMode(RequestMode requestMode) {
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

    public void setIndependentTask(boolean independentTask) {
        isIndependentTask = independentTask;
    }

    public boolean isIndependentTask() {
        return isIndependentTask;
    }

    public void setRespondData(byte[] respondData) {
        this.respondData = respondData;
    }

    public byte[] getRespondData() {
        return respondData;
    }

    protected void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    protected void setResponseProperty(Map<String, List<String>> responseProperty) {
        this.responseProperty = responseProperty;
    }

    public void setException(Throwable e) {
        this.exception = e;
    }

    public Map<String, List<String>> getResponseProperty() {
        return responseProperty;
    }

    public Throwable getException() {
        return exception;
    }
}
