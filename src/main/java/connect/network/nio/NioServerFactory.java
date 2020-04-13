package connect.network.nio;


import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.NioSSLFactory;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerFactory extends AbsNetFactory<NioServerTask> {

    private static NioServerFactory mFactory = null;

    public static synchronized INetFactory<NioServerTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioServerFactory();
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

    public NioServerFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioEngine(getNetWork());
    }

    @Override
    protected BaseNetWork<NioServerTask> initNetWork() {
        return new NioServerWork(getSslFactory());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new NioSSLFactory();
    }

    @Override
    public ISSLFactory getSslFactory() {
        return super.getSslFactory();
    }

    @Override
    public boolean addTask(NioServerTask task) {
        if (task != null && task.getSelectionKey() == null && !task.isTaskNeedClose()) {
            return super.addTask(task);
        }
        return false;
    }

    @Override
    public void removeTask(NioServerTask task) {
        if (task != null && task.getSelectionKey() != null && !task.isTaskNeedClose()) {
            super.removeTask(task);
        }
    }


    @Override
    public void close() {
        super.close();
        NioServerWork nioNetWork = getNetWork();
        if (nioNetWork != null && nioNetWork.getSelector() != null) {
            nioNetWork.getSelector().wakeup();
        }
    }
}
