package com.jav.net.nio;

import com.jav.net.entity.MultiByteBuffer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpSender extends AbsNioCacheNetSender<NioUdpSender.SenderPacket> {

    protected DatagramChannel mChannel;

    public static final int MAX_LENGTH = 65506;


    public static class SenderPacket {
        public final SocketAddress mAddress;
        public final MultiByteBuffer mData;

        public SenderPacket(SocketAddress address, MultiByteBuffer data) {
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
    protected int onHandleSendData(Object data) throws Throwable {
        if (data instanceof SenderPacket) {
            SenderPacket packet = (SenderPacket) data;
            if (packet.mAddress == null) {
                return super.onHandleSendData(packet.mData);
            }
            int ret = sendUdpDataImp(packet);
            if (ret == SEND_CHANNEL_BUSY) {
                mCacheComponent.addFirstData(data);
            } else if (ret == SEND_COMPLETE) {
                packet.mData.clear();
            }
            return ret;
        }
        return SEND_COMPLETE;
    }

    @Override
    protected int sendDataImp(ByteBuffer[] buffers) {
        if (mChannel == null || buffers == null || !mChannel.isOpen()) {
            return SEND_FAIL;
        }
        for (ByteBuffer buffer : buffers) {
            try {
                int realLength = 0;
                if (buffer.limit() > MAX_LENGTH) {
                    realLength = buffer.limit();
                    buffer.limit(MAX_LENGTH);
                }
                while (mChannel.isOpen()) {
                    int ret = mChannel.write(buffer);
                    if (ret < 0) {
                        return SEND_FAIL;
                    } else if (ret == 0 && mChannel.isOpen()) {
                        return buffer.hasRemaining() ? SEND_CHANNEL_BUSY : SEND_FAIL;
                    }
                    realLength -= ret;
                    if (realLength > 0) {
                        if (realLength > MAX_LENGTH) {
                            buffer.limit(buffer.position() + MAX_LENGTH);
                        } else {
                            buffer.limit(buffer.position() + realLength);
                        }
                    } else {
                        break;
                    }
                }
            } catch (Throwable e) {
                return SEND_FAIL;
            }
        }
        return SEND_COMPLETE;
    }

    protected int sendUdpDataImp(SenderPacket packet) throws Throwable {
        if (mChannel == null || packet == null || !mChannel.isOpen() || packet.mData == null || packet.mAddress == null) {
            return SEND_FAIL;
        }
        ByteBuffer[] sendDataBuf = packet.mData.getTmpBuf();
        if (sendDataBuf == null) {
            sendDataBuf = packet.mData.getUseBuf(true);
        }
        for (ByteBuffer buffer : sendDataBuf) {
            int realLength = 0;
            if (buffer.limit() > MAX_LENGTH) {
                realLength = buffer.limit();
                buffer.limit(MAX_LENGTH);
            }
            while (buffer.hasRemaining() && mChannel.isOpen()) {
                long ret = mChannel.send(buffer, packet.mAddress);
                if (ret < 0) {
                    return SEND_FAIL;
                } else if (ret == 0 && mChannel.isOpen() && buffer.hasRemaining()) {
                    packet.mData.setBackBuf(sendDataBuf);
                    return buffer.hasRemaining() ? SEND_CHANNEL_BUSY : SEND_FAIL;
                }
                if (realLength > 0) {
                    realLength -= ret;
                    if (realLength > MAX_LENGTH) {
                        buffer.limit(buffer.position() + MAX_LENGTH);
                    } else {
                        buffer.limit(buffer.position() + realLength);
                    }
                }
            }
        }
        return SEND_COMPLETE;
    }

}
