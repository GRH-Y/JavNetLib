package connect.network.nio;

import connect.network.base.AbsNetEngine;
import connect.network.base.joggle.INetFactory;

public class NioHPCClientFactory extends NioClientFactory {


    private static INetFactory<NioClientTask> mFactory = null;

    private NioHPCClientFactory() {
    }

    private NioHPCClientFactory(int threadCount) {
        super();
        NioHighPcEngine engine = getNetEngine();
        engine.setThreadCount(threadCount);
    }

    public static synchronized INetFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioHPCClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static synchronized INetFactory<NioClientTask> getFactory(int threadCount) {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioHPCClientFactory(threadCount);
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
        return new NioHighPcEngine(this, getNetWork());
    }
}
