package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.SSLFactory;

public class TcpServerFactory extends AbsNetFactory<TcpServerTask> {

    private static TcpServerFactory mFactory;

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioServerWork(this);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }


    public static synchronized TcpServerFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpServerFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpServerFactory();
                }
            }
        }
        return mFactory;
    }
}
