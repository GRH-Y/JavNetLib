package connect.network.base;

import connect.network.base.joggle.IFactory;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioSimpleClientFactory;

public class HighPcClientFactory {


    private static IFactory<NioClientTask> mFactory = null;

    private HighPcClientFactory() {
    }

    public static synchronized IFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (HighPcClientFactory.class) {
                if (mFactory == null) {
                    NioSimpleClientFactory factory = new NioSimpleClientFactory();
                    HighPcEngine highPcFactoryEngine = new HighPcEngine(factory);
                    factory.setEngine(highPcFactoryEngine);
                    mFactory = factory;
                }
            }
        }
        return mFactory;
    }

    public static synchronized IFactory<NioClientTask> getFactory(HighPcEngine engine) {
        if (mFactory == null) {
            synchronized (HighPcClientFactory.class) {
                if (mFactory == null && engine != null) {
                    NioSimpleClientFactory factory = new NioSimpleClientFactory();
                    engine.setFactory(factory);
                    factory.setEngine(engine);
                    mFactory = factory;
                }
            }
        }
        return mFactory;
    }

    public static synchronized IFactory<NioClientTask> getFactory(int threadCount) {
        if (mFactory == null) {
            synchronized (HighPcClientFactory.class) {
                if (mFactory == null) {
                    NioSimpleClientFactory factory = new NioSimpleClientFactory();
                    HighPcEngine highPcFactoryEngine = new HighPcEngine(factory);
                    highPcFactoryEngine.setThreadCount(threadCount);
                    factory.setEngine(highPcFactoryEngine);
                    mFactory = factory;
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
