package connect.network.aio;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.SSLFactory;

public class AioClientFactory extends AbsNetFactory<AioClientTask> {

    private static INetFactory<AioClientTask> mFactory = null;

    public static synchronized INetFactory<AioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (AioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new AioClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new AioEngine(getNetWork());
    }

    @Override
    protected BaseNetWork<AioClientTask> initNetWork() {
        return new AioClientNetWork<>(getSSLFactory(), this);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }

    @Override
    public boolean addTask(AioClientTask task) {
        if (task != null) {
            return super.addTask(task);
        }
        return false;
    }

    @Override
    public void removeTask(AioClientTask task) {
        if (task != null) {
            super.removeTask(task);
        }
    }
}
