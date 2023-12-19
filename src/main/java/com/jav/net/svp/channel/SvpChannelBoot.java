package com.jav.net.svp.channel;

import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioUdpFactory;
import com.jav.net.nio.NioUdpTask;

public class SvpChannelBoot {

    private SvpChannelContext mContext;

    private NioUdpFactory mUdpFactory;

    private static final class InnerClass {
        public static final SvpChannelBoot sBoot = new SvpChannelBoot();
    }

    public static SvpChannelBoot getInstance() {
        return InnerClass.sBoot;
    }

    private SvpChannelBoot() {

    }

    public void init(SvpChannelContext context) {
        mContext = context;
    }

    private void initChannel() {
        if (mUdpFactory != null || mContext == null) {
            return;
        }
        mUdpFactory = new NioUdpFactory();
        mUdpFactory.open();
        SvpChannel svpChannel = new SvpChannel();
        if (mContext.isServerMode()) {
            svpChannel.bindAddress(mContext.getHost(), mContext.getPort());
        } else {
            svpChannel.setAddress(mContext.getHost(), mContext.getPort());
        }
        INetTaskComponent<NioUdpTask> component = mUdpFactory.getNetTaskComponent();
        component.addExecTask(svpChannel);
    }

    public void boot() {
        initChannel();
    }

    public void unBoot() {
        if (mUdpFactory != null) {
            mUdpFactory.close();
            mUdpFactory = null;
        }
    }
}
