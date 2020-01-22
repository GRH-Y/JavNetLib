package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;

public class TcpClientFactory<T extends TcpClientTask> extends AbsNetFactory<T> {

    private static TcpClientFactory mFactory;

    private static ISSLFactory mSslFactory = null;

    public TcpClientFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(this, getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioClientWork<T>(this);
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


    public static void setSSLFactory(ISSLFactory sslFactory) {
        if (sslFactory != null) {
            mSslFactory = sslFactory;
        }
    }

    protected ISSLFactory getSslFactory() {
        return mSslFactory;
    }

}
