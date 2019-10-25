package connect.network.nio;


import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;

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

    private NioServerFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioEngine(this, getNetWork());
    }

    @Override
    protected BaseNetWork<NioServerTask> initNetWork() {
        return new NioServerWork(this);
    }

    @Override
    public void addTask(NioServerTask task) {
        if (task != null && task.getSelectionKey() == null && !task.isTaskNeedClose()) {
            super.addTask(task);
        }
    }

    @Override
    public void removeTask(NioServerTask task) {
        if (task != null && task.getSelectionKey() != null && !task.isTaskNeedClose()) {
            super.removeTask(task);
        }
    }

    @Override
    protected void removeTaskInside(NioServerTask task, boolean isNeedWakeup) {
        super.removeTaskInside(task, isNeedWakeup);
    }

    @Override
    public void close() {
        NioEngine nioEngine = getNetEngine();
        nioEngine.setNeedStop();
        NioServerWork nioNetWork = getNetWork();
        if (nioNetWork != null && nioNetWork.getSelector() != null) {
            nioNetWork.getSelector().wakeup();
        }
        super.close();
    }
}
