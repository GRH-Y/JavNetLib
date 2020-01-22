package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;

public class TcpServerFactory extends AbsNetFactory<TcpServerTask> {
    private static TcpServerFactory mFactory;

    private ISSLFactory mSslFactory = null;

    public TcpServerFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(this, getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioServerWork(this);
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

    public void setSSlFactory(ISSLFactory sslFactory) {
        if (sslFactory != null) {
            this.mSslFactory = sslFactory;
        }
    }

    protected ISSLFactory getSslFactory() {
        return mSslFactory;
    }
}
