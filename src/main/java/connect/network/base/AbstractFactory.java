package connect.network.base;

import connect.network.base.joggle.IFactory;
import connect.network.base.joggle.ISSLFactory;
import task.executor.joggle.IConsumerTaskExecutor;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbstractFactory<T> implements IFactory<T> {

    protected FactoryCoreTask coreTask;


    abstract protected boolean onConnectTask(T task);

    abstract protected void onExecTask(T task);

    abstract protected void onDisconnectTask(T task);


    protected void setFactoryCoreTask(FactoryCoreTask coreTask) {
        this.coreTask = coreTask;
    }

    @Override
    public void addTask(T task) {
        if (task != null && coreTask != null && coreTask.executor.getLoopState() && !coreTask.mConnectCache.contains(task)) {
            coreTask.mConnectCache.add(task);
            wakeUpCoreTask();
        }
    }

    @Override
    public void removeTask(T task) {
        if (task != null && coreTask != null && coreTask.executor.getLoopState()) {
            coreTask.removeNeedDestroyTask(task);
            wakeUpCoreTask();
        }
    }

    @Override
    public void setSSlFactory(ISSLFactory sslFactory) {
    }

    @Override
    public void open() {
        if (coreTask == null) {
            coreTask = new FactoryCoreTask(this);
        }
        if (!coreTask.executor.isStartState()) {
            coreTask.executor.startTask();
        }
    }

    @Override
    public void openHighPer() {
        if (coreTask != null) {
            IConsumerTaskExecutor executor = coreTask.container.getTaskExecutor();
            executor.startAsyncProcessData();
        }
    }

    @Override
    public void close() {
        if (coreTask != null) {
            coreTask.executor.blockStopTask();
        }
    }


    protected void wakeUpCoreTask() {
        if (coreTask != null) {
            coreTask.executor.resumeTask();
        }
    }

}
