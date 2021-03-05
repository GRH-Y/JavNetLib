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
        sslFactory = initSSLFactory();

        mWork = initNetWork();
        if (mWork == null) {
            throw new IllegalStateException("initNetWork() This method returns value can not be null");
        }
        mEngine = initNetEngine();
        if (mEngine == null) {
            throw new IllegalStateException("initNetEngine() This method returns value can not be null");
        }
    }

    abstract protected AbsNetEngine initNetEngine();

    abstract protected BaseNetWork<T> initNetWork();

    abstract protected ISSLFactory initSSLFactory();

    public ISSLFactory getSslFactory() {
        return sslFactory;
    }

    protected <T extends AbsNetEngine> T getNetEngine() {
        return (T) mEngine;
    }

    protected <T extends BaseNetWork> T getNetWork() {
        return (T) mWork;
    }

    //-----------------------------------------------------------------------------------------


    @Override
    public boolean addTask(T task) {
        boolean ret = false;
        if (task.getTaskStatus() == NetTaskStatus.NONE) {
            ret = mEngine.isEngineRunning();
            if (ret) {
                ret = mWork.addConnectTask(task);
                if (ret) {
                    mEngine.resumeEngine();
                }
            }
        }
        return ret;
    }

    @Override
    public void removeTask(T task) {
        if (task.getTaskStatus() == NetTaskStatus.RUN) {
            boolean ret = mEngine.isEngineRunning();
            if (ret) {
                ret = mWork.addDestroyTask(task);
                if (ret) {
                    mEngine.resumeEngine();
                }
            }
        }
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

    @Override
    public boolean isOpen() {
        if (mEngine == null) {
            return false;
        }
        return mEngine.isEngineRunning();
    }
}
