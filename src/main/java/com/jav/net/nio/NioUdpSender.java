package com.jav.net.nio;

import com.jav.net.base.MultiBuffer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpSender extends AbsNioCacheNetSender<NioUdpSender.SenderPacket> {

    protected DatagramChannel mChannel;

    /**
     * 最大的udp单包大小
     */
    public static final int MAX_PACK_LENGTH = 1440;


    public static class SenderPacket {
        public final SocketAddress mAddress;
        public final MultiBuffer mData;

        public SenderPacket(SocketAddress address, MultiBuffer data) {
            this.mAddress = address;
            this.mData = data;
        }
    }

    public void setChannel(SelectionKey selectionKey, DatagramChannel channel) {
        if (selectionKey == null || channel == null) {
            throw new NullPointerException("selectionKey or channel is null !!!");
        }
        this.mSelectionKey = selectionKey;
        this.mChannel = channel;
    }


    @Override
    protected int onHandleSendData(NioUdpSender.SenderPacket packet) throws Throwable {
        int ret = sendDataImp(packet);
        if (ret == SEND_CHANNEL_BUSY) {
            mCacheComponent.addFirstData(packet);
        } else if (ret == SEND_COMPLETE) {
            packet.mData.clear();
        }
        return ret;
    }

    @Override
    protected int sendDataImp(Object data) throws IOException {
        if (mChannel == null || data == null || !mChannel.isOpen()) {
            return SEND_FAIL;
        }
        NioUdpSender.SenderPacket packet = null;
        if (data instanceof NioUdpSender.SenderPacket) {
            packet = (SenderPacket) data;
        }
        if (packet == null) {
            return SEND_FAIL;
        }
        ByteBuffer[] sendDataBuf = packet.mData.getFreezeBuf();
        if (sendDataBuf == null) {
            sendDataBuf = packet.mData.getDirtyBuf(true);
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
                if (packet.mAddress == null) {
                    ret = mChannel.write(buffer);
                } else {
                    ret = mChannel.send(buffer, packet.mAddress);
                }
                if (ret < 0) {
                    return SEND_FAIL;
                } else if (ret == 0 && mChannel.isOpen() && buffer.hasRemaining()) {
                    packet.mData.restoredBuf(sendDataBuf);
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
