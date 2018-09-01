package connect.network.http;

/**
 * 请求类型
 */
public enum ConnectType {

    POST("POST"), GET("GET");

    private String type;

    ConnectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
