public class BaseRequest {

    private int code;

    private String message;

    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }

    public String getContent() {
        return data;
    }

    public void setContent(String content) {
        this.data = content;
    }

    @Override
    public String toString() {
        return "BaseResponseBean{" +
                "code=" + code +
                ", msg='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
