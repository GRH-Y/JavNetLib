package connect.network.http;

/**
 * 请求类型
 */
public enum RequestMode {

    POST("POST"), GET("GET");

    private String mode;

    RequestMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
