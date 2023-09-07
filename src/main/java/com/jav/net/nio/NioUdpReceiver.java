package com.jav.net.nio;

import com.jav.net.base.AbsNetReceiver;
import com.jav.net.base.UdpPacket;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpReceiver extends AbsNetReceiver<DatagramChannel, UdpPacket> {


    @Override
    protected void onReadNetData(DatagramChannel channel) throws Throwable {
        ByteBuffer buffer = ByteBuffer.allocate(NioUdpSender.MAX_PACK_LENGTH);
        SocketAddress address = channel.receive(buffer);
        if (mReceiver != null) {
            mReceiver.onReceiveFullData(new UdpPacket(address, buffer));
        }
    }
}
