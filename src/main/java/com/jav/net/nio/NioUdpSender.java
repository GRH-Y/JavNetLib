package com.jav.net.nio;

import com.jav.net.base.MultiBuffer;
import com.jav.net.base.UdpPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpSender extends AbsNioCacheNetSender<DatagramChannel, UdpPacket> {


    /**
     * 最大的udp单包大小
     */
    public static final int MAX_PACK_LENGTH = 1440;


    @Override
    protected int onHandleSendData(UdpPacket packet) throws Throwable {
        int ret = sendDataImp(packet);
        if (ret == SEND_CHANNEL_BUSY) {
            mCacheComponent.addFirstData(packet);
        } else if (ret == SEND_COMPLETE) {
            packet.getUdpData().clear();
        }
        return ret;
    }

    @Override
    protected int sendDataImp(Object data) throws IOException {
        if (mChannel == null || data == null || !mChannel.isOpen()) {
            return SEND_FAIL;
        }
        UdpPacket packet = null;
        if (data instanceof UdpPacket) {
            packet = (UdpPacket) data;
        }
        if (packet == null) {
            return SEND_FAIL;
        }
        MultiBuffer udpData = packet.getUdpData();
        ByteBuffer[] sendDataBuf = udpData.getFreezeBuf();
        if (sendDataBuf == null) {
            sendDataBuf = udpData.getDirtyBuf(true);
        }
        for (ByteBuffer buffer : sendDataBuf) {
            if (!buffer.hasRemaining()) {
                continue;
            }
            int realLength = 0;
            if (buffer.limit() > MAX_PACK_LENGTH) {
                realLength = buffer.limit();
                buffer.limit(MAX_PACK_LENGTH);
            }
            while (buffer.hasRemaining() && mChannel.isOpen()) {
                long ret;
                if (packet.getAddress() == null) {
                    ret = mChannel.write(buffer);
                } else {
                    ret = mChannel.send(buffer, packet.getAddress());
                }
                if (ret < 0) {
                    return SEND_FAIL;
                } else if (ret == 0 && mChannel.isOpen() && buffer.hasRemaining()) {
                    udpData.restoredBuf(sendDataBuf);
                    return buffer.hasRemaining() ? SEND_CHANNEL_BUSY : SEND_FAIL;
                }
                if (realLength > 0) {
                    realLength -= ret;
                    if (realLength > MAX_PACK_LENGTH) {
                        buffer.limit(buffer.position() + MAX_PACK_LENGTH);
                    } else {
                        buffer.limit(buffer.position() + realLength);
                    }
                }
            }
        }
        return SEND_COMPLETE;
    }

}
