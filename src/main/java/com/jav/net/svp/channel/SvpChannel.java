package com.jav.net.svp.channel;

import com.jav.net.nio.NioUdpReceiver;
import com.jav.net.nio.NioUdpSender;
import com.jav.net.nio.NioUdpTask;
import com.jav.net.svp.channel.joggle.ICombinedPackCompleteListener;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class SvpChannel extends NioUdpTask implements ICombinedPackCompleteListener {

    protected SvpPackDispatch mDispatch;

    @Override
    protected void onBeReadyChannel(SelectionKey selectionKey, DatagramChannel channel) {
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
    public void onRequestPack(ChannelInfo channelInfo) {

    }

    @Override
    public void onCombinedCompletePack(ChannelInfo channelInfo, ByteBuffer fullData) {
        if (channelInfo.isTcp()) {
            SvpTcpClient svpTcpClient = new SvpTcpClient(channelInfo, fullData);

        }
    }
}
