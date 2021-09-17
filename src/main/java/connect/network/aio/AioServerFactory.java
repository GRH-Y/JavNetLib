package connect.network.aio;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.SSLFactory;

public class AioServerFactory extends AbsNetFactory<AioServerTask> {

    private static AioServerFactory mFactory = null;

    public static synchronized INetFactory<AioServerTask> getFactory() {
        if (mFactory == null) {
            synchronized (AioServerFactory.class) {
                if (mFactory == null) {
                    mFactory = new AioServerFactory();
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
    protected BaseNetWork<AioServerTask> initNetWork() {
        return new AioServerNetWork(getSSLFactory(), this);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }


    @Override
    public boolean addTask(AioServerTask task) {
        if (task != null) {
            return super.addTask(task);
        }
        return false;
    }

    @Override
    public void removeTask(AioServerTask task) {
        if (task != null) {
            super.removeTask(task);
        }
    }
}
