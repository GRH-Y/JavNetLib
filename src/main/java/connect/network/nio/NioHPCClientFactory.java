package connect.network.nio;

import connect.network.base.NioHighPcEngine;
import connect.network.base.joggle.IFactory;

public class NioHPCClientFactory {


    private static IFactory<NioClientTask> mFactory = null;

    private NioHPCClientFactory() {
    }

    public static synchronized IFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioSimpleClientFactory(new NioHighPcEngine());
                }
            }
        }
        return mFactory;
    }

    public static synchronized IFactory<NioClientTask> getFactory(NioHighPcEngine engine) {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null && engine != null) {
                    mFactory = new NioSimpleClientFactory(engine);
                }
            }
        }
        return mFactory;
    }

    public static synchronized IFactory<NioClientTask> getFactory(int threadCount) {
        if (mFactory == null) {
            synchronized (NioHPCClientFactory.class) {
                if (mFactory == null) {
                    NioHighPcEngine highPcFactoryEngine = new NioHighPcEngine();
                    highPcFactoryEngine.setThreadCount(threadCount);
                    mFactory = new NioSimpleClientFactory(highPcFactoryEngine);
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
