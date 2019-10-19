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
    public void addTask(NioClientTask task) {
        NioClientWork nioNetWork = getNetWork();
        if (task == null || nioNetWork == null || nioNetWork.getSelector() == null) {
            return;
        }
        if (task.getSelectionKey() != null && !task.isTaskNeedClose()) {
            return;
        }
        super.addTask(task);
    }

    @Override
    public void removeTask(NioClientTask task) {
        NioClientWork nioNetWork = getNetWork();
        if (task == null || nioNetWork == null || nioNetWork.getSelector() == null) {
            return;
        }
        if (task.getSelectionKey() == null || task.isTaskNeedClose()) {
            return;
        }
        super.removeTask(task);
    }

    @Override
    public void close() {
        super.close();
        NioClientWork nioNetWork = getNetWork();
        if (nioNetWork != null && nioNetWork.getSelector() != null) {
            nioNetWork.getSelector().wakeup();
        }
    }

}
