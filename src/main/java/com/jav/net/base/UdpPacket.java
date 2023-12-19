package com.jav.net.base;

import java.net.SocketAddress;

/**
 * udp数据包
 *
 * @author no.8
 */
public class UdpPacket {
    /**
     * 数据源地址信息
     */
    private SocketAddress mAddress;

    /**
     * 接收到的完整数据
     */
    private MultiBuffer mFullData;

    public UdpPacket(SocketAddress address, MultiBuffer fullData) {
        mAddress = address;
        mFullData = fullData;
    }

    /**
     * 获取接收到的完整数据
     *
     * @return
     */
    public MultiBuffer getUdpData() {
        return mFullData;
    }

    /**
     * 获取请求端的地址信息
     *
     * @return
     */
    public SocketAddress getAddress() {
        return mAddress;
    }
}