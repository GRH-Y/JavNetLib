package connect.network.base;

import connect.network.base.joggle.INetFactory;
import connect.network.base.joggle.ISSLFactory;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbsNetFactory<T extends BaseNetTask> implements INetFactory<T> {

    private AbsNetEngine mEngine;

    private BaseNetWork<T> mWork;

    private ISSLFactory sslFactory;

    protected AbsNetFactory() {
        mWork = initNetWork();
        if (mWork == null) {
            throw new IllegalStateException(" initNetWork() This method returns value can not be null");
        }
        mEngine = initNetEngine();
        if (mEngine == null) {
            throw new IllegalStateException(" initNetEngine() This method returns value can not be null");
        }
        try {
            sslFactory = initSSLFactory();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    abstract protected AbsNetEngine initNetEngine();

    abstract protected BaseNetWork<T> initNetWork();

    abstract protected ISSLFactory initSSLFactory() throws Exception;

    protected ISSLFactory getSslFactory() {
        return sslFactory;
    }

    protected <T extends AbsNetEngine> T getNetEngine() {
        return (T) mEngine;
    }

    protected <T extends BaseNetWork> T getNetWork() {
        return (T) mWork;
    }

    //-----------------------------------------------------------------------------------------

    private boolean checkException(T task) {
        if (task == null) {
            return false;
        }
        if (!mEngine.isEngineRunning()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean addTask(T task) {
        boolean ret = checkException(task);
        if (ret) {
            ret = mWork.addConnectTask(task);
        }
        if (ret) {
            mEngine.resumeEngine();
        }
        return ret;
    }

    @Override
    public void removeTask(T task) {
        removeTaskInside(task, true);
    }

    protected boolean removeTaskInside(T task, boolean isNeedWakeup) {
        boolean ret = checkException(task);
        if (ret) {
            task.setTaskNeedClose(true);
        }
        //该任务在此线程才能添加
        if (ret) {
            ret = mWork.addDestroyTask(task);
        }
        if (isNeedWakeup && ret) {
            mEngine.resumeEngine();
        }
        return ret;
    }

    //-----------------------------------------------------------------------------------------

    @Override
    public void open() {
        mEngine.startEngine();
    }

    @Override
    public void close() {
        mEngine.stopEngine();
    }
}
