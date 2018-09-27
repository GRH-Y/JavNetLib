package connect.network.nio;


import connect.network.base.joggle.IFactory;
import task.executor.BaseLoopTask;
import task.executor.LoopTaskExecutor;
import task.executor.TaskContainer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractNioFactory<T> implements IFactory<T> {

    protected volatile Queue<CoreTask> mExecutorQueue;
    protected volatile Queue<T> mConnectCache;
    protected volatile Queue<T> mDestroyCache;

    private boolean mIsNeedDestroy = true;
    private Selector mSelector;

    private long mLoadTime = 0;//2000000000

    public AbstractNioFactory() {
        mConnectCache = new ConcurrentLinkedQueue<>();
        mExecutorQueue = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    /**
     * 设置处理线程负荷时间
     *
     * @param loadTime 毫秒
     */
    public void setLoadTime(int loadTime) {
        if (mLoadTime > 0) {
            this.mLoadTime = loadTime * 1000000L;
        }
    }

    abstract protected void onConnectTask(Selector selector, T task);

    abstract protected void onSelectorTask(Selector selector);

    abstract protected void onDisconnectTask(T task);


    @Override
    public void addTask(T task) {
        if (task != null && !mExecutorQueue.isEmpty() && !mConnectCache.contains(task)) {
            for (SelectionKey selectionKey : mSelector.selectedKeys()) {
                T hasTask = (T) selectionKey.attachment();
                if (hasTask == task) {
                    return;
                }
            }
            mConnectCache.add(task);
            mSelector.wakeup();
            wakeUpCoreTask();
        }
    }

    @Override
    public void removeTask(T task) {
        if (task != null && !mExecutorQueue.isEmpty() && !mDestroyCache.contains(task)) {
            mDestroyCache.add(task);
            mSelector.wakeup();
            wakeUpCoreTask();
        }
    }

    @Override
    public void open() {
        if (mExecutorQueue.isEmpty()) {
            openCoreTask();
        }
    }

    @Override
    public void close() {
        setIsNeedDestroy(true);
        destroyCoreTask();
    }

    /**
     * @param status
     */
    protected void setIsNeedDestroy(boolean status) {
        mIsNeedDestroy = status;
    }

    protected void wakeUpCoreTask() {
        for (CoreTask coreTask : mExecutorQueue) {
            coreTask.getExecutor().resumeTask();
        }
    }

    /**
     * 开启新的线程
     */
    protected void openCoreTask() {
        if (mSelector == null) {
            try {
                mSelector = Selector.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CoreTask coreTask = new CoreTask();
        coreTask.getExecutor().startTask();
        mExecutorQueue.add(coreTask);
    }

    protected void destroyCoreTask() {
        mExecutorQueue.forEach(item -> {
            item.getExecutor().stopTask();
            mSelector.wakeup();
            item.getExecutor().blockStopTask();
        });
//        while (!mExecutorQueue.isEmpty()) {
//            CoreTask coreTask = mExecutorQueue.remove();
//            coreTask.getExecutor().stopTask();
//            mSelector.wakeup();
//            coreTask.getExecutor().blockStopTask();
//        }
        mExecutorQueue.clear();
        mConnectCache.clear();
        try {
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected class CoreTask extends BaseLoopTask {

        private TaskContainer container;
        private LoopTaskExecutor executor;

        private CoreTask() {
            container = new TaskContainer(this);
            executor = container.getTaskExecutor();
        }

        public LoopTaskExecutor getExecutor() {
            return executor;
        }

        private void removeNeedDestroyNioTask() {
            //销毁链接
            while (!mDestroyCache.isEmpty()) {
                T target = mDestroyCache.remove();
                onDisconnectTask(target);
                Iterator<SelectionKey> iterator = mSelector.keys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    T task = (T) selectionKey.attachment();
                    if (task == target) {
                        try {
                            selectionKey.channel().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        selectionKey.cancel();
                        break;
                    }
                }
            }
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
                onConnectTask(mSelector, task);
            }

            int count = 0;
            try {
                count = mSelector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (count > 0) {
                onSelectorTask(mSelector);
            }

            //清除要结束的任务
            removeNeedDestroyNioTask();

            if (mLoadTime == 0) {
                return;
            }

            long useTime = System.nanoTime() - startTime;
            if (useTime > mLoadTime) {
                //执行速度过慢，则另外开启线程!
//                Logcat.d("==> 执行速度过慢，则另外开启线程! " + useTime);
                openCoreTask();
            } else if (useTime < 1000000 && mExecutorQueue.size() > 1) {
                //少于1毫秒而且线程数据大于1，则另外关闭多余线程!
                setIsNeedDestroy(false);
                getExecutor().stopTask();
                mExecutorQueue.remove(this);
            }
        }

        @Override
        protected void onDestroyTask() {
            //线程结束，是否要调用任务生命周期onClose方法
            if (mIsNeedDestroy) {
                for (SelectionKey selectionKey : mSelector.keys()) {
                    T task = (T) selectionKey.attachment();
                    onDisconnectTask(task);
                    selectionKey.cancel();
                }
            }
        }
    }
}
