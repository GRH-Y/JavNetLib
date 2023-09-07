package com.jav.net.base;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * udp数据包
 *
 * @author no.8
 */
public class UdpPacket {
    /**
     * 请求端的地址信息
     */
    private SocketAddress mRequestAdr;

    /**
     * 接收到的完整数据
     */
    private ByteBuffer mFullData;

    public UdpPacket(SocketAddress reqAdr, ByteBuffer fullData) {
        mRequestAdr = reqAdr;
        mFullData = fullData;
    }

    /**
     * 获取接收到的完整数据
     *
     * @return
     */
    public ByteBuffer getFullData() {
        return mFullData;
    }

    /**
     * 获取请求端的地址信息
     *
     * @return
     */
    public SocketAddress getRequestAdr() {
        return mRequestAdr;
    }
}