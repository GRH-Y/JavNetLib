package connect.network.base;

import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 单线程engine
 *
 * @param <T>
 */
public class LowPcEngine<T extends BaseNetTask> extends FactoryEngine<T> {

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;

    /**
     * 销毁任务队列
     */
    protected Queue<T> mDestroyCache;

    protected ILoopTaskExecutor mExecutor = null;
    protected ITaskContainer mContainer = null;

    public LowPcEngine() {
        mConnectCache = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void addTask(T task) {
        if (task != null && !mConnectCache.contains(task)) {
            mConnectCache.add(task);
            if (mExecutor != null) {
                mExecutor.resumeTask();
            }
        }
    }

    @Override
    protected void removeTask(T task) {
        if (task != null && !mDestroyCache.contains(task)) {
            //该任务在此线程才能添加
            mDestroyCache.add(task);
            if (mExecutor != null) {
                mExecutor.resumeTask();
            }
        }
    }

    @Override
    protected void startEngine() {
        if (mContainer == null) {
            mContainer = TaskExecutorPoolManager.getInstance().createJThread(this);
            mExecutor = mContainer.getTaskExecutor();
        }
        mExecutor.startTask();
    }

    @Override
    protected void stopEngine() {
        if (mExecutor != null) {
            mExecutor.blockStopTask();
        }
    }
}
