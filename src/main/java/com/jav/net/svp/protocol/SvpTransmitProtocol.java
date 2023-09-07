package com.jav.net.svp.protocol;

import java.nio.ByteBuffer;


/**
 * Svp 的 init 协议构建类
 *
 * @author no.8
 */
public class SvpTransmitProtocol {


    /**
     * 创建 Transmit 协议的请求数据包
     *
     * @param passId  init 协议返回的数据
     * @param srcPort 请求端的端口
     * @param index   该数据包的顺序值
     * @param data    转发的数据
     * @return
     */
    public static ByteBuffer getRequestHead(byte[] passId, short srcPort, short index, byte[] data) {
        if (srcPort <= 0 || index < 0 || data == null) {
            return null;
        }
        if (passId == null || passId.length != ISvpProtocol.PASS_ID_LENGTH) {
            return null;
        }

        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.TRANSMIT_HEAD_LENGTH + data.length);
        //time
        head.putLong(System.currentTimeMillis());
        //flags
        head.put(SvpFlags.TRANSMIT);
        //pass_id
        head.put(passId);
        // req_id
        head.putShort(srcPort);
        //pack_id
        head.putLong(System.currentTimeMillis());
        //index
        head.putShort(index);
        //data
        head.put(data);

        return head;
    }

    /**
     * 创建 Transmit 协议的响应数据包
     *
     * @param passId  init 协议返回的数据
     * @param srcPort 请求端的端口
     * @param packId  包的id，记录管咯分包, Request 请求过来带有
     * @param index   该数据包的顺序值
     * @return
     */
    public static ByteBuffer getResponseHead(byte[] passId, short srcPort, long packId, short index) {
        if (passId == null || passId.length != ISvpProtocol.PASS_ID_LENGTH
                || srcPort <= 0 || packId <= 0 || index < 0) {
            return null;
        }

        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.TRANSMIT_HEAD_LENGTH);
        //time
        head.putLong(System.currentTimeMillis());
        //flags
        head.put((byte) (SvpFlags.TRANSMIT | SvpFlags.ACK));
        //pass_id
        head.put(passId);
        // req_id
        head.putShort(srcPort);
        //pack_id
        head.putLong(packId);
        //index
        head.putShort(index);

        return head;
    }

}
