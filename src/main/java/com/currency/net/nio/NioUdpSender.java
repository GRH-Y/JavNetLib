package com.currency.net.nio;

import com.currency.net.base.AbsNetSender;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioUdpSender extends AbsNetSender {

    protected DatagramChannel mChannel;

    public NioUdpSender() {
    }

    public NioUdpSender(DatagramChannel channel) {
        setChannel(channel);
    }

    public void setChannel(DatagramChannel channel) {
        if (channel == null) {
            throw new NullPointerException("channel is null !!!");
        }
        this.mChannel = channel;
    }


    @Override
    public void sendData(Object objData) {
        if (objData instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer) objData;
            try {
                mChannel.write(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
