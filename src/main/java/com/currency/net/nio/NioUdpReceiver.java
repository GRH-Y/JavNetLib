package com.currency.net.nio;

import com.currency.net.base.AbsNetReceiver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpReceiver extends AbsNetReceiver<DatagramChannel, NioUdpReceiver.ReceiverPacket> {

    private static final int PACKET_MAX_SIZE = 1472;

    private ByteBuffer mBuffer = ByteBuffer.allocate(PACKET_MAX_SIZE);

    public static class ReceiverPacket {
        public SocketAddress mFromAddress;
        public byte[] mData;

        public ReceiverPacket(SocketAddress fromAddress, byte[] data) {
            this.mFromAddress = fromAddress;
            this.mData = data;
        }
    }

    protected void onReadNetData(DatagramChannel channel) throws Throwable {
        SocketAddress address = channel.receive(mBuffer);
        if (mReceiverCallBack != null) {
            byte[] data = null;
            if (mBuffer.limit() > 0) {
                mBuffer.flip();
                data = new byte[mBuffer.limit()];
                mBuffer.get(data);
            }
            mReceiverCallBack.onReceiveFullData(new ReceiverPacket(address, data), null);
        }
    }
}
