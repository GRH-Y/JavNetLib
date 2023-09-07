package com.jav.net.svp.protocol;

public interface SvpFlags {

    /**
     * 初始化flags，校验mid，返回pass_id
     */
    byte INIT = 1;

    /**
     * 请求flags，记录接下来传输包的数量，要链接的目标地址和端口
     */
    byte REQUEST = 2;


    /**
     * 传输flags,真正传输数据
     */
    byte TRANSMIT = 4;


    /**
     * 确定flags,服务端响应确认收到数据
     */
    byte ACK = 8;

    /**
     * tcp的数据包flags
     */
    byte TCP = 16;

    /**
     * udp的数据包flags
     */
    byte UDP = 32;

    /**
     * 错误位，如果该位置1 表示请求出错
     */
    byte ERROR = 64;
}
