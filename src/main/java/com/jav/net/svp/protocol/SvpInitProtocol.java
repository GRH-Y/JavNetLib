package com.jav.net.svp.protocol;

import java.nio.ByteBuffer;

/**
 * Svp的Request协议构建类
 *
 * @author no.8
 */
public class SvpInitProtocol {

    public static ByteBuffer getRequestHead(byte[] rsaMid) {
        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.INIT_HEAD_LENGTH);
        head.putLong(System.currentTimeMillis());
        head.put(SvpFlags.INIT);
        head.put(rsaMid);
        return head;
    }

    public static ByteBuffer getResponseHead(byte[] rsaPassId) {
        ByteBuffer head = ByteBuffer.allocate(ISvpProtocol.INIT_HEAD_LENGTH);
        head.putLong(System.currentTimeMillis());
        head.put((byte) (SvpFlags.INIT | SvpFlags.ACK));
        head.put(rsaPassId);
        return head;
    }

}
