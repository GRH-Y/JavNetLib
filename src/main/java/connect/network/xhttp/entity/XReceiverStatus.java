package connect.network.xhttp.entity;

public enum XReceiverStatus {
    /**
     * 当前解析head
     */
    HEAD,
    /**
     * 当前解析body
     */
    BODY,
    /**
     * 解析完毕
     */
    OVER,
    /**
     * 数据回调完毕
     */
    NONE
}
