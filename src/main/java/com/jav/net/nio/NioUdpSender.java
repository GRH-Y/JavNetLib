package com.jav.net.nio;

import com.jav.net.entity.MultiByteBuffer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpSender extends AbsNioCacheNetSender<NioUdpSender.SenderPacket> {

    protected DatagramChannel mChannel;


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
                return super.onHandleSendData(data);
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
    protected int sendDataImp(ByteBuffer[] buffers) throws IOException {
        if (mChannel == null || buffers == null || !mChannel.isOpen()) {
            return SEND_FAIL;
        }
        do {
            try {
                long ret = mChannel.write(buffers);
                if (ret < 0) {
                    return SEND_FAIL;
                } else if (ret == 0 && mChannel.isOpen()) {
                    return hasRemaining(buffers) ? SEND_CHANNEL_BUSY : SEND_FAIL;
                }
            } catch (Throwable e) {
                return SEND_FAIL;
            }
        } while (hasRemaining(buffers) && mChannel.isOpen());
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
        do {
            for (ByteBuffer buffer : sendDataBuf) {
                long ret = mChannel.send(buffer, packet.mAddress);
                if (ret < 0) {
                    throw new IOException("## failed to send data. The socket channel may be closed !!! ");
                } else if (ret == 0 && packet.mData.hasRemaining() && mChannel.isOpen()) {
                    packet.mData.setBackBuf(sendDataBuf);
                    return SEND_CHANNEL_BUSY;
                }
            }
        } while (hasRemaining(sendDataBuf) && mChannel.isOpen());
        return SEND_COMPLETE;
    }

}
