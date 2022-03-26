package com.currency.net.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpSender extends AbsNioCacheNetSender {

    protected DatagramChannel mChannel;


    public static class SenderPacket {
        public final SocketAddress mAddress;
        public final ByteBuffer mData;

        public SenderPacket(SocketAddress address, ByteBuffer data) {
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
    protected Object onCheckAndChangeData(Object objData) {
        if (objData instanceof SenderPacket) {
            return objData;
        }
        return super.onCheckAndChangeData(objData);
    }

    @Override
    protected int onHandleSendData(Object data) throws Throwable {
        if (data instanceof SenderPacket) {
            SenderPacket packet = (SenderPacket) data;
            int ret = sendUdpDataImp(packet);
            if (ret == SEND_CHANNEL_BUSY) {
                mDataQueue.addFirst(data);
            }
            return ret;
        }
        return super.onHandleSendData(data);
    }

    @Override
    protected int sendDataImp(ByteBuffer buffers) throws Throwable {
        if (mChannel == null || buffers == null || !mChannel.isOpen()) {
            return SEND_FAIL;
        }
        do {
            long ret = mChannel.write(buffers);
            if (ret < 0) {
                throw new IOException("## failed to send data. The socket channel may be closed !!! ");
            } else if (ret == 0 && buffers.hasRemaining() && mChannel.isOpen()) {
                return SEND_CHANNEL_BUSY;
            }
        } while (buffers.hasRemaining() && mChannel.isOpen());
        return SEND_COMPLETE;
    }

    protected int sendUdpDataImp(SenderPacket packet) throws Throwable {
        if (mChannel == null || packet == null || !mChannel.isOpen() || packet.mData == null || packet.mAddress == null) {
            return SEND_FAIL;
        }
        do {
            long ret = mChannel.send(packet.mData, packet.mAddress);
            if (ret < 0) {
                throw new IOException("## failed to send data. The socket channel may be closed !!! ");
            } else if (ret == 0 && packet.mData.hasRemaining() && mChannel.isOpen()) {
                return SEND_CHANNEL_BUSY;
            }
        } while (packet.mData.hasRemaining() && mChannel.isOpen());
        return SEND_COMPLETE;
    }

}
