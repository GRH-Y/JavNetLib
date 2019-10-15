package connect.network.nio;

import connect.network.base.NioHighPcEngine;
import connect.network.base.joggle.INetFactory;

public class NioHPCClientFactory {


    private static INetFactory<NioClientTask> mFactory = null;

    private NioHPCClientFactory() {
    }

    public static synchronized INetFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioSyncClientFactory(new NioHighPcEngine());
                }
            }
        }
        return mFactory;
    }

    public static synchronized INetFactory<NioClientTask> getFactory(NioHighPcEngine engine) {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null && engine != null) {
                    mFactory = new NioSyncClientFactory(engine);
                }
            }
        }
        return mFactory;
    }

    public static synchronized INetFactory<NioClientTask> getFactory(int threadCount) {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    NioHighPcEngine highPcFactoryEngine = new NioHighPcEngine();
                    highPcFactoryEngine.setThreadCount(threadCount);
                    mFactory = new NioSyncClientFactory(highPcFactoryEngine);
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
}
