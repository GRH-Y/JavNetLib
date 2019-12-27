package connect.network.nio;


import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.INetFactory;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory extends AbsNetFactory<NioClientTask> {

    private static INetFactory<NioClientTask> mFactory = null;

    protected NioClientFactory() {
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


    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioEngine(this, getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioClientWork(this);
    }


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

    @Override
    protected void removeTaskInside(NioClientTask task, boolean isNeedWakeup) {
        super.removeTaskInside(task, isNeedWakeup);
    }

    @Override
    public void close() {
        NioEngine nioEngine = getNetEngine();
        nioEngine.setNeedStop();
        NioClientWork nioNetWork = getNetWork();
        if (nioNetWork != null && nioNetWork.getSelector() != null) {
            nioNetWork.getSelector().wakeup();
        }
        super.close();
    }

}
