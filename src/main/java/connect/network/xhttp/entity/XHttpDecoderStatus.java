package connect.network.xhttp.entity;

public enum XHttpDecoderStatus {
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
     * 未知状态
     */
    NONE
}
