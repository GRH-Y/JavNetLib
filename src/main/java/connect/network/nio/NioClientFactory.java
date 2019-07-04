package connect.network.nio;


import connect.network.base.NioEngine;
import connect.network.base.joggle.IFactory;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory {

    private static IFactory<NioClientTask> mFactory = null;

    private NioClientFactory() {
    }

    public static synchronized IFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioSimpleClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static synchronized IFactory<NioClientTask> getFactory(NioEngine engine) {
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
