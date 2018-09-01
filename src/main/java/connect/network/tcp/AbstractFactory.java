package connect.network.tcp;

import connect.network.base.joggle.IFactory;
import task.executor.BaseLoopTask;
import task.executor.LoopTaskExecutor;
import task.executor.TaskContainer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractFactory<T> implements IFactory<T> {

    protected volatile Queue<CoreTask> mExecutorQueue;
    protected volatile Queue<T> mConnectCache;


    private boolean mIsNeedDestroy = false;

    private long mLoadTime = 2000000000;

    public AbstractFactory() {
        mConnectCache = new ConcurrentLinkedQueue<>();
        mExecutorQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 设置处理线程负荷时间
     *
     * @param loadTime 毫秒
     */
    public void setLoadTime(int loadTime) {
        if (loadTime > 0) {
            this.mLoadTime = loadTime * 1000000L;
        }
    }

    @Override
    public void addTask(T task) {
        if (task == null || mExecutorQueue.isEmpty()) {
            return;
        }
        mConnectCache.add(task);
        wakeUpCoreTask();
    }

    @Override
    public void removeTask(T task) {
        if (task == null || mExecutorQueue.isEmpty()) {
            return;
        }
        for (CoreTask coreTask : mExecutorQueue) {
            coreTask.removeNeedDestroyTask(task);
        }
        wakeUpCoreTask();
    }

    @Override
    public void open() {
        if (mExecutorQueue.isEmpty()) {
            openCoreTask();
        }
    }

    @Override
    public void close() {
        setNeedDestroy(true);
        destroyCoreTask();
    }

    abstract protected boolean onConnectTask(T task);

    abstract protected void onExecTask(T task);

    abstract protected void onDisconnectTask(T task);

    /**
     * 开启新的线程
     */
    protected void openCoreTask() {
        CoreTask coreTask = new CoreTask();
        coreTask.getExecutor().startTask();
        mExecutorQueue.add(coreTask);
    }

    protected void wakeUpCoreTask() {
        for (CoreTask coreTask : mExecutorQueue) {
            coreTask.getExecutor().resumeTask();
        }
    }

    /**
     * @param status
     */
    protected void setNeedDestroy(boolean status) {
        mIsNeedDestroy = status;
    }


    protected void destroyCoreTask() {
        while (!mExecutorQueue.isEmpty()) {
            CoreTask coreTask = mExecutorQueue.remove();
            coreTask.getExecutor().stopTask();
        }
        mConnectCache.clear();
    }


    private class CoreTask extends BaseLoopTask {

        private TaskContainer container;
        private LoopTaskExecutor executor;
        private volatile Queue<T> mRunCache;
        private volatile Queue<T> mDestroyCache;

        CoreTask() {
            container = new TaskContainer(this);
            executor = container.getTaskExecutor();
            mRunCache = new ConcurrentLinkedQueue<>();
            mDestroyCache = new ConcurrentLinkedQueue<>();
        }

        protected LoopTaskExecutor getExecutor() {
            return executor;
        }

        private void onRemoveNeedDestroyTask() {
            //销毁链接
            while (!mDestroyCache.isEmpty()) {
                T task = mDestroyCache.remove();
                mRunCache.remove(task);
                onDisconnectTask(task);
            }
        }

        protected void removeNeedDestroyTask(T task) {
            mDestroyCache.add(task);
        }

        @Override
        protected void onRunLoopTask() {
            long startTime = 0;
            if (mLoadTime != 0) {
                startTime = System.nanoTime();
            }

            //检测是否有新的任务添加
            while (!mConnectCache.isEmpty()) {
                T task = mConnectCache.remove();
                boolean isConnect = onConnectTask(task);
                if (isConnect) {
                    mRunCache.add(task);
                } else {
                    mDestroyCache.add(task);
                }
            }

            for (T task : mRunCache) {
                onExecTask(task);
            }

            //清除要结束的任务
            onRemoveNeedDestroyTask();

            if (mLoadTime == 0) {
                return;
            }

            long useTime = System.nanoTime() - startTime;
            if (useTime > mLoadTime) {
                openCoreTask();
            } else if (mLoadTime > useTime && useTime > 50000 && mExecutorQueue.size() > 1) {
                //Logcat.d("==> 任务压力不大执行速度过快，则另外关闭多余线程! " + useTime);
                setNeedDestroy(false);
                getExecutor().stopTask();
                mExecutorQueue.remove(this);
            } else {
                getExecutor().sleepTask(1000);
            }

        }

        @Override
        protected void onDestroyTask() {
            //清除要结束的任务
            onRemoveNeedDestroyTask();

            if (mIsNeedDestroy) {
                while (!mRunCache.isEmpty()) {
                    T task = mRunCache.remove();
                    onDisconnectTask(task);
                }
            }
        }
    }


}
