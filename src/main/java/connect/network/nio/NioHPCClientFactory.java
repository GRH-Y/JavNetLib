package connect.network.nio;

import connect.network.base.AbsNetEngine;
import connect.network.base.joggle.INetFactory;

public class NioHPCClientFactory extends NioClientFactory {


    private static INetFactory<NioClientTask> mFactory = null;


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

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioHighPcEngine(getNetWork());
    }
}
