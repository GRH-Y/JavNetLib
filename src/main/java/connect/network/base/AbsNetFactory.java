package connect.network.base;

import connect.network.base.joggle.INetFactory;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbsNetFactory<T extends BaseNetTask> implements INetFactory<T> {

    private AbsNetEngine mEngine;

    private BaseNetWork<T> mWork;

    protected AbsNetFactory() {
        mWork = initNetWork();
        if (mWork == null) {
            throw new IllegalStateException(" initNetWork() This method returns value can not be null");
        }
        mEngine = initNetEngine();
        if (mEngine == null) {
            throw new IllegalStateException(" initNetEngine() This method returns value can not be null");
        }
    }

    abstract protected AbsNetEngine initNetEngine();

    abstract protected BaseNetWork<T> initNetWork();

    protected <T extends AbsNetEngine> T getNetEngine() {
        return (T) mEngine;
    }

    protected <T extends BaseNetWork> T getNetWork() {
        return (T) mWork;
    }

    //-----------------------------------------------------------------------------------------

    private void checkException(T task) {
        if (task == null) {
            throw new NullPointerException("Task can not be null !!!");
        }
        if (!mEngine.isEngineRunning()) {
            throw new IllegalStateException("Engine not started,Check if the open() method is not called !!!");
        }
    }

    @Override
    public boolean addTask(T task) {
        checkException(task);
        boolean ret = mWork.addConnectTask(task);
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
        checkException(task);
        task.setTaskNeedClose(true);
        //该任务在此线程才能添加
        boolean ret = mWork.addDestroyTask(task);
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
