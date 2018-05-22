package connect.network.nio;


import connect.network.nio.interfaces.INioNetTask;
import connect.network.nio.interfaces.INioSelectorFactory;
import task.executor.BaseLoopTask;
import task.executor.LoopTaskExecutor;
import task.executor.TaskContainer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractNioFactory<T> implements INioSelectorFactory<T> {

    protected Queue<CoreTask> executorQueue;
    protected Queue<T> connectCache;
    protected Queue<INioNetTask> destroyCache;

    private boolean isNeedDestroy = true;
    private Selector selector;

    private final static int TIME_OUT_SECOND = 2000000000;

    public AbstractNioFactory() throws IOException {
        selector = Selector.open();
        connectCache = new ConcurrentLinkedQueue<>();
        executorQueue = new ConcurrentLinkedQueue<>();
        destroyCache = new ConcurrentLinkedQueue<>();
    }

    abstract protected void onHasTask(Selector selector, T task);

    abstract protected void onSelector(Selector selector);


    @Override
    public void addNioTask(T task) {
        if (task == null || executorQueue.size() == 0) {
            return;
        }

        connectCache.add(task);
        selector.wakeup();
        wakeUpCoreTask();
    }

    @Override
    public void removeNioTask(T task) {
        if (task == null || executorQueue.size() == 0) {
            return;
        }
        destroyCache.add((INioNetTask) task);
        selector.wakeup();
        wakeUpCoreTask();
    }

    @Override
    public void open() {
        if (executorQueue != null && executorQueue.size() == 0) {
            openCoreTask();
        }
    }

    @Override
    public void close() {
        setNeedDestroy(false);
        destroyCoreTask();
    }

    /**
     * 
     * @param status
     */
    protected void setNeedDestroy(boolean status) {
        isNeedDestroy = status;
    }

    protected void wakeUpCoreTask() {
        for (CoreTask coreTask : executorQueue) {
            coreTask.getExecutor().resumeTask();
        }
    }

    /**
     * 开启新的线程
     */
    protected void openCoreTask() {
        CoreTask coreTask = new CoreTask();
        coreTask.getExecutor().startTask();
        executorQueue.add(coreTask);
    }

    protected void destroyCoreTask() {
        while (executorQueue != null && !executorQueue.isEmpty()) {
            CoreTask coreTask = executorQueue.remove();
            if (coreTask != null) {
                coreTask.getExecutor().stopTask();
            }
        }

        if (connectCache != null) {
            connectCache.clear();
            connectCache = null;
        }

        if (selector != null) {
            selector.wakeup();
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            selector = null;
        }
    }


    private void removeNeedDestroyNioTask() {
        //销毁链接
        while (!destroyCache.isEmpty()) {
            INioNetTask task = destroyCache.remove();
            task.onClose();
            AbstractSelectableChannel channel = task.getSocketChannel();
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                channel.keyFor(selector).cancel();
            }
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

        @Override
        protected void onRunLoopTask() {

            //检测是否有新的任务添加
            while (!connectCache.isEmpty()) {
                T task = connectCache.remove();
                if (task != null) {
                    onHasTask(selector, task);
                }
            }

            int count = 0;
            try {
//                Logcat.d("==> AbstractNioFactory onRunLoopTask start select()");
                count = selector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Logcat.d("==> AbstractNioFactory onRunLoopTask end select()");

            long startTime = System.nanoTime();

            if (count > 0) {
                onSelector(selector);
            }

            long useTime = System.nanoTime() - startTime;
            if (useTime > TIME_OUT_SECOND) {
                //执行速度过慢，则另外开启线程!
//                Logcat.d("==> 执行速度过慢，则另外开启线程! " + useTime);
                openCoreTask();
            } else if (TIME_OUT_SECOND > useTime && useTime > 50000) {
                //任务压力不大执行速度过快，则另外关闭多余线程!
                if (executorQueue.size() > 1) {
//                    Logcat.d("==> 任务压力不大执行速度过快，则另外关闭多余线程! " + useTime);
                    setNeedDestroy(false);
                    getExecutor().stopTask();
                    executorQueue.remove(this);
                }
            } else {
//                Logcat.d("==> 任务压力不大执行速度过快,线程需要睡眠2000毫秒! " + useTime);
                getExecutor().sleepTask(1000);
            }

            //清除要结束的任务
            removeNeedDestroyNioTask();
        }

        @Override
        protected void onDestroyTask() {
            //线程结束，是否要调用任务生命周期onClose方法
            if (isNeedDestroy) {
                Set<SelectionKey> keySet = selector.keys();
                for (SelectionKey selectionKey : keySet) {
                    selectionKey.cancel();
                    INioNetTask task = (INioNetTask) selectionKey.attachment();
                    task.onClose();
                }
            }
        }
    }
}
