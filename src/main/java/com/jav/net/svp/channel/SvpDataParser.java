package com.jav.net.svp.channel;

import com.jav.net.svp.protocol.ISvpProtocol;
import com.jav.net.svp.protocol.SvpFlags;

import java.nio.ByteBuffer;

public class SvpDataParser {

    /**
     * time 的起始地址
     */
    private static final int INDEX_TIME = 0;

    /**
     * flags 的起始地址
     */
    private static final int INDEX_FLAGS = 8;

    /**
     * pass_id 的起始地址
     */
    private static final int INDEX_PASS_ID = 9;

    /**
     * req_id 的起始地址
     */
    private static final int INDEX_REQ_ID = 25;

    /**
     * pack_id 的起始地址
     */
    private static final int INDEX_PACK_ID = 27;

    /**
     * count 的起始地址
     */
    private static final int INDEX_COUNT = 35;

    /**
     * count 的起始地址
     */
    private static final int INDEX_PORT = 37;

    /**
     * adr 的起始地址
     */
    private static final int INDEX_ADR = 39;

    /**
     * index 的起始地址
     */
    private static final int INDEX_INDEX = 35;

    /**
     * INIT 类型包的内容起始地址
     */
    private static final int INDEX_INIT_DATA = 9;

    /**
     * TRANSMIT 类型包的内容起始地址
     */
    private static final int INDEX_TRANSMIT_DATA = 37;

    private SvpDataParser() {
    }


    /**
     * 获取时间，记录没个数据包创建的当前时间
     *
     * @param fullData
     * @return
     */
    public static long getTime(ByteBuffer fullData) {
        fullData.position(INDEX_TIME);
        return fullData.getLong();
    }

    /**
     * 数据包的flags，记录该包需要做的操作，什么类型的数据包
     *
     * @param fullData
     * @return
     */
    public static byte getFlags(ByteBuffer fullData) {
        fullData.position(INDEX_FLAGS);
        return fullData.get();
    }

    /**
     * 判断当前数据包响应是否错误
     *
     * @param flags 数据的第9位是flags位
     * @return true 表示有错
     */
    public static boolean isError(byte flags) {
        return (flags & SvpFlags.ERROR) == SvpFlags.ERROR;
    }


    /**
     * 判断当前数据包响应是否错误
     *
     * @return
     */
    public static byte[] getPassId(ByteBuffer fullData) {
        fullData.position(INDEX_PASS_ID);
        byte[] passId = new byte[ISvpProtocol.PASS_ID_LENGTH];
        fullData.get(passId);
        return passId;
    }

    /**
     * 获取req_id
     *
     * @return
     */
    public static short getReqId(ByteBuffer fullData) {
        fullData.position(INDEX_REQ_ID);
        return fullData.getShort();
    }

    /**
     * 获取pack_id
     *
     * @return
     */
    public static long getPackId(ByteBuffer fullData) {
        fullData.position(INDEX_PACK_ID);
        return fullData.getLong();
    }

    /**
     * 获取count
     *
     * @return
     */
    public static short getPackCount(ByteBuffer fullData) {
        fullData.position(INDEX_COUNT);
        return fullData.getShort();
    }

    /**
     * 获取目标端口号
     *
     * @return
     */
    public static short getPort(ByteBuffer fullData) {
        fullData.position(INDEX_PORT);
        return fullData.getShort();
    }

    /**
     * 获取目标端口号
     *
     * @return
     */
    public static byte[] getAddress(ByteBuffer fullData) {
        fullData.position(INDEX_ADR);
        byte[] adr = new byte[ISvpProtocol.ADDRESS_LENGTH];
        fullData.get(adr);
        return adr;
    }

    /**
     * 获取 index
     *
     * @return
     */
    public static short getPackIndex(ByteBuffer fullData) {
        fullData.position(INDEX_INDEX);
        return fullData.getShort();
    }

    /**
     * 获取数据包里面的数据内容
     *
     * @param fullData
     * @return
     */
    public static byte[] getContent(byte flags, ByteBuffer fullData) {
        if ((flags & SvpFlags.INIT) == SvpFlags.INIT) {
            // 当前数据包是 INIT 包
            fullData.position(INDEX_INIT_DATA);
        } else if ((flags & SvpFlags.REQUEST) == SvpFlags.REQUEST) {
            // 当前数据包是 REQUEST 包 ,该数据包只有head信息
            return null;
        } else if ((flags & SvpFlags.TRANSMIT) == SvpFlags.TRANSMIT) {
            // 当前数据包是 TRANSMIT 包
            fullData.position(INDEX_TRANSMIT_DATA);
        } else {
            //非法的flags
            return null;
        }
        byte[] content = new byte[fullData.limit() - fullData.position()];
        fullData.get(content);
        return content;
    }
}
