package com.jav.net.nio;

import com.jav.net.base.AbsNetReceiver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpReceiver extends AbsNetReceiver<DatagramChannel, NioUdpReceiver.ReceiverPacket> {

    private static final int PACKET_MAX_SIZE = 65535;

    public static class ReceiverPacket {
        public final SocketAddress mFromAddress;
        public ByteBuffer mData;

        public ReceiverPacket(SocketAddress fromAddress, ByteBuffer data) {
            this.mFromAddress = fromAddress;
            this.mData = data;
        }
    }

    @Override
    protected void onReadNetData(DatagramChannel channel) throws Throwable {
        ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);
        SocketAddress address = channel.receive(buffer);
        if (mReceiverCallBack != null) {
            mReceiverCallBack.onReceiveFullData(new ReceiverPacket(address, buffer), null);
        }
    }
}
