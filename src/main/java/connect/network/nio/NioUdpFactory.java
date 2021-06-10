package connect.network.nio;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;

public class NioUdpFactory extends AbsNetFactory<NioUdpTask> {

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioEngine(getNetWork());
    }

    @Override
    protected BaseNetWork<NioUdpTask> initNetWork() {
        return new NioUdpWork();
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return null;
    }
}
