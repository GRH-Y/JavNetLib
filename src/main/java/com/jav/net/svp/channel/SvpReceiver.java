package com.jav.net.svp.channel;

import com.jav.net.base.UdpPacket;
import com.jav.net.base.joggle.INetReceiver;
import com.jav.net.nio.NioUdpReceiver;

public class SvpReceiver {

    private final CoreReceiver mReceiver;

    private class CoreReceiver extends NioUdpReceiver implements INetReceiver<UdpPacket> {


        private CoreReceiver() {
            setDataReceiver(this);
        }


        @Override
        public void onReceiveFullData(UdpPacket packet) {

        }

        @Override
        public void onReceiveError(Throwable e) {

        }
    }

    public SvpReceiver() {
        mReceiver = new CoreReceiver();
    }


    public CoreReceiver getReceiver() {
        return mReceiver;
    }
}
