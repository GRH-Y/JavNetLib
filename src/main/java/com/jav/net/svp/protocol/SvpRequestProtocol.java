package com.jav.net.svp.protocol;

import java.nio.ByteBuffer;

/**
 * Svp的Request协议构建类
 *
 * @author no.8
 */
public class SvpRequestProtocol {

    private SvpRequestProtocol() {
    }


    /**
     * 创建 Request 协议的请求数据包 （注意 dataLength 大于44g count的16位数据不能表示）
     *
     * @param isTcp      是否是tcp协议的数据
     * @param packLength transmit 单包多大，用于拆包计算，默认是1440
     * @param dataLength transmit 要传输数据的总大小
     * @param passId     init 协议返回的数据
     * @param packId     pack id 区拆包归属哪个组
     * @param address    要链接的目标地址
     * @param port       要链接的目标端口
     * @return
     */
    public static ByteBuffer getRequestHead(boolean isTcp, int packLength, int dataLength, byte[] passId, long packId,
                                            short reqId, byte[] address, short port) {
        if (port <= 0 || dataLength <= 0 || reqId <= 0) {
            return null;
        }
        if (passId == null || passId.length != ISvpProtocol.PASS_ID_LENGTH) {
            return null;
        }
        if (address == null || address.length != ISvpProtocol.ADDRESS_LENGTH) {
            return null;
        }

        //计算出拆包需要多少个分包才能发送完成
        int packCount = dataLength / (packLength - ISvpProtocol.TRANSMIT_HEAD_LENGTH);

        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.REQUEST_HEAD_LENGTH);
        //time
        head.putLong(System.currentTimeMillis());
        //flags
        byte type = isTcp ? SvpFlags.TCP : SvpFlags.UDP;
        head.put((byte) (SvpFlags.REQUEST | type));
        //pass_id
        head.put(passId);
        // req_id
        head.putShort(reqId);
        //pack_id
        head.putLong(packId);
        //count
        head.putShort((short) packCount);
        //port
        head.putShort(port);
        //addr
        head.put(address);
        return head;
    }

    /**
     * 创建 Request 协议的响应数据包
     *
     * @param passId  init 协议返回的数据
     * @param srcPort 请求端的端口
     * @param packId  包的id，记录管咯分包, Request 请求过来带有
     * @return
     */
    public static ByteBuffer getResponseHead(byte[] passId, short srcPort, long packId) {
        if (passId == null || passId.length != ISvpProtocol.PASS_ID_LENGTH || srcPort <= 0) {
            return null;
        }

        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.REQUEST_ANSWER_HEAD_LENGTH);
        //time
        head.putLong(System.currentTimeMillis());
        //flags
        head.put((byte) (SvpFlags.REQUEST | SvpFlags.ACK));
        //pass_id
        head.put(passId);
        // req_id
        head.putShort(srcPort);
        //pack_id
        head.putLong(packId);

        return head;
    }

}
