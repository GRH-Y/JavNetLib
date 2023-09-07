package com.jav.net.svp.channel;

import com.jav.net.nio.NioUdpReceiver;
import com.jav.net.nio.NioUdpSender;
import com.jav.net.nio.NioUdpTask;
import com.jav.net.svp.channel.joggle.ICombinedPackCompleteListener;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class SvpServer extends NioUdpTask implements ICombinedPackCompleteListener {

    protected SvpPackDispatch mDispatch;

    @Override
    protected void onBeReadyChannel(DatagramChannel channel) {
        mDispatch = new SvpPackDispatch(this);
        NioUdpReceiver svpReceiver = new NioUdpReceiver();
        svpReceiver.setDataReceiver(mDispatch);
        setReceiver(svpReceiver);

        NioUdpSender svpSender = new NioUdpSender();
        setSender(svpSender);
    }

    @Override
    protected void onCloseChannel() {
        mDispatch.stopDispatch();
    }

    @Override
    public void onRequestPack(byte[] adr, int port) {

    }

    @Override
    public void onCombinedCompletePack(ByteBuffer pack) {

    }
}
