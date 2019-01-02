package connect.network.base;

import task.executor.BaseConsumerTask;
import task.executor.LoopTaskExecutor;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ITaskContainer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class FactoryCoreTask<T> extends BaseConsumerTask<T> {

    protected ITaskContainer container;
    protected LoopTaskExecutor executor;
    protected AbstractFactory mFactory;

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;
    /**
     * 正在执行任务的队列
     */
    protected Queue<T> mExecutorQueue;
    /**
     * 销毁任务队列
     */
    protected volatile Queue<T> mDestroyCache;


    public FactoryCoreTask(AbstractFactory factory) {
        this.mFactory = factory;
        container = TaskExecutorPoolManager.getInstance().createJThread(this);
        executor = container.getTaskExecutor();
        mConnectCache = new ConcurrentLinkedQueue<>();
        mExecutorQueue = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }


    protected void onRemoveNeedDestroyTask() {
        //销毁链接
        while (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            try {
                mFactory.onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mExecutorQueue.remove(task);
        }
    }


    public void removeNeedDestroyTask(T task) {
        if (!mDestroyCache.contains(task)) {
            //该任务在此线程才能添加
            mDestroyCache.add(task);
        }
    }

    private void execTask() {
        //执行任务
        for (T task : mExecutorQueue) {
            if (executor.getLoopState()) {
                try {
                    mFactory.onExecTask(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    @Override
    protected void onCreateData() {

        //检测是否有新的任务添加
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            try {
                boolean isConnect = mFactory.onConnectTask(task);
                if (isConnect) {
                    mExecutorQueue.add(task);
                } else {
                    mDestroyCache.add(task);
                }
            } catch (Exception e) {
                mDestroyCache.add(task);
                e.printStackTrace();
            }
        }

        execTask();

        //清除要结束的任务
        onRemoveNeedDestroyTask();
    }

    @Override
    protected void onProcess() {
        execTask();
    }


    @Override
    protected void onDestroyTask() {
        //清除要结束的任务
        onRemoveNeedDestroyTask();
        //主动结束所有的任务
        while (!mExecutorQueue.isEmpty()) {
            T task = mExecutorQueue.remove();
            try {
                mFactory.onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mConnectCache.clear();
        mExecutorQueue.clear();
        mDestroyCache.clear();
    }
}
