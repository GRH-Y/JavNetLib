package connect.network.nio;

import connect.network.base.AbsNetFactory;
import log.LogDog;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskContainer;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ITaskContainer;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 多线程engine
 *
 * @param <T>
 */
public class NioHighPcEngine<T extends BaseNioNetTask> extends NioEngine {

    private Map<String, ITaskContainer> engineMap;

    private int threadCount = 4;

    private long rootEngineTag = 0;

    private IConsumerAttribute<SelectionKey> attribute = null;

    public NioHighPcEngine(AbsNetFactory factory, NioNetWork<T> work) {
        super(factory, work);
    }

    public void setThreadCount(int threadCount) {
        if (threadCount > 0) {
            this.threadCount = threadCount;
        }
    }

    @Override
    protected boolean isEngineRunning() {
        if (engineMap.isEmpty()) {
            return false;
        }
        ITaskContainer container = engineMap.get(String.valueOf(rootEngineTag));
        return container.getTaskExecutor().getLoopState();
    }

    @Override
    protected void onEngineRun() {
        if (threadCount == 1) {
            //如果只开启一条线程则不以默认方式运行
            super.onRunLoopTask();
        } else {
            //如果是主引擎处理事件分发
            if (Thread.currentThread().getId() == rootEngineTag) {
                //检测是否有新的任务添加
                mWork.onCheckConnectTask(false);
                //检查是否有事件任务
                LogDog.d("==> 主引擎");
                onEventDistribution();
                //清除要结束的任务
                mWork.onCheckRemoverTask(false);
            } else {
                LogDog.d("==> 非主引擎");
                //检测是否有新的任务添加
//                mFactory.onCheckConnectTask(false);
                //提取要处理的事件
                SelectionKey selectionKey = attribute.popCacheData();
                if (selectionKey != null) {
                    mWork.onSelectionKey(selectionKey);
                }
                //清除要结束的任务
//                mWork.onCheckRemoverTask(false);
                //引擎休眠
                waitEngine();
            }
        }
    }


    /**
     * 分发事件
     */
    private void onEventDistribution() {
        int count = 0;
        try {
            count = mWork.getSelector().select();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mWork.getSelector().selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                attribute.pushToCache(selectionKey);
            }
            wakeUpOtherEngine();
        }
    }

    /**
     * 唤醒非主引擎
     */
    private void wakeUpOtherEngine() {
        for (ITaskContainer container : engineMap.values()) {
            if (container.getThread().getId() != rootEngineTag) {
                container.getTaskExecutor().resumeTask();
            }
        }
    }

    /**
     * 引擎休眠
     */
    private void waitEngine() {
        ITaskContainer container = engineMap.get(String.valueOf(Thread.currentThread().getId()));
        container.getTaskExecutor().waitTask(0);
    }

    @Override
    protected void resumeEngine() {
        super.resumeEngine();
        wakeUpOtherEngine();
    }

    @Override
    protected void startEngine() {
        if (engineMap == null) {
            engineMap = new HashMap<>(threadCount);
        }
        if (engineMap.isEmpty()) {
            attribute = new ConsumerQueueAttribute();
            for (int count = 0; count < threadCount; count++) {
                ITaskContainer container = new TaskContainer(this);
                if (count == 0) {
                    //把第一个线程定为主引擎，只负责事件处理
                    rootEngineTag = container.getThread().getId();
                }
                container.getTaskExecutor().startTask();
                engineMap.put(String.valueOf(container.getThread().getId()), container);
            }
        }
    }

    @Override
    protected void stopEngine() {
        if (engineMap != null && !engineMap.isEmpty()) {
            for (ITaskContainer container : engineMap.values()) {
                TaskExecutorPoolManager.getInstance().destroy(container);
            }
            engineMap.clear();
        }
    }

}
