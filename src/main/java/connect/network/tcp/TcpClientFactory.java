package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.SSLFactory;

public class TcpClientFactory<T extends TcpClientTask> extends AbsNetFactory<T> {

    private static TcpClientFactory mFactory;

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioClientWork<T>(this);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }

    public synchronized static TcpClientFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpClientFactory();
                }
            }
        }
        return mFactory;
    }

}
