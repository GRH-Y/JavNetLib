package connect.network.nio;


import connect.network.base.NioEngine;
import connect.network.base.joggle.INetFactory;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory {

    private static INetFactory<NioClientTask> mFactory = null;

    private NioClientFactory() {
    }

    public static synchronized INetFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioSimpleClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static synchronized INetFactory<NioClientTask> getFactory(NioEngine engine) {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioSimpleClientFactory(engine);
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
