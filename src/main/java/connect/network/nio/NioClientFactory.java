package connect.network.nio;


import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.NioSSLFactory;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory extends AbsNetFactory<NioClientTask> {

    private static INetFactory<NioClientTask> mFactory = null;

    public NioClientFactory() {
    }

    public static synchronized INetFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioClientFactory();
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


    //--------------------------- start init-----------------------------------------------
    @Override
    protected ISSLFactory initSSLFactory() {
        return new NioSSLFactory();
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioClientWork(getSslFactory());
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioEngine(getNetWork());
    }

    //--------------------------- end init-----------------------------------------------


    @Override
    public boolean addTask(NioClientTask task) {
        if (task != null && task.getSelectionKey() == null && !task.isTaskNeedClose()) {
            return super.addTask(task);
        }
        return false;
    }

    @Override
    public void removeTask(NioClientTask task) {
        if (task != null && task.getSelectionKey() != null && !task.isTaskNeedClose()) {
            super.removeTask(task);
        }
    }
}
