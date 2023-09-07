package com.jav.net.svp.protocol;

/**
 * Svp协议常量值
 *
 * @author no.8
 */
public interface ISvpProtocol {

    /**
     * init 协议数据包头长度
     */
    int INIT_HEAD_LENGTH = 9;

    /**
     * request 协议的请求数据包头长度
     */
    int REQUEST_HEAD_LENGTH = 43;

    /**
     * request 协议的响应包头长度
     */
    int REQUEST_ANSWER_HEAD_LENGTH = 35;

    /**
     * Transmit 协议的数据包头长度
     */
    int TRANSMIT_HEAD_LENGTH = 37;

    /**
     * pass_id 字段的长度
     */
    int PASS_ID_LENGTH = 16;

    /**
     * adr 字段的长度
     */
    int ADDRESS_LENGTH = 4;

    /**
     * transmit 单包大小，默认是1440
     */
    int DEFAULT_TRANSMIT_DATA_LENGTH = 1440;
}
