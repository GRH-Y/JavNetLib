package connect.network.base;

import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * 多线程engine
 *
 * @param <T>
 */
public class HighPcEngine<T extends BaseNetTask> extends FactoryEngine<T> {

    private FactoryEngine mEngine;
//    private NetMultiTask multiTask = null;

    private List<ITaskContainer> taskContainerList;

    private int threadCount = 4;


    public HighPcEngine() {
    }

    public HighPcEngine(AbstractFactory factory) {
        setFactory(factory);
    }

    public void setFactory(AbstractFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory is null !!!");
        }
        this.mEngine = factory.getEngine();
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    protected void onRunLoopTask() {
        mEngine.onRunLoopTask();
    }

    @Override
    protected void startEngine() {
        taskContainerList = new ArrayList<>(threadCount);
        for (int count = 0; count < threadCount; count++) {
            ITaskContainer container = TaskExecutorPoolManager.getInstance().createJThread(this);
            container.getTaskExecutor().startTask();
            taskContainerList.add(container);
        }
//        if (multiTask == null) {
//            multiTask = new NetMultiTask(threadCount, this);
//        }
//        multiTask.startExec();
    }

    @Override
    protected void stopEngine() {
        for (ITaskContainer container : taskContainerList) {
            container.getTaskExecutor().blockStopTask();
        }
        taskContainerList.clear();
//        if (multiTask != null) {
//            multiTask.stopExec();
//        }
    }

    private void wakeUp() {
        for (ITaskContainer container : taskContainerList) {
            container.getTaskExecutor().resumeTask();
        }
    }

    @Override
    protected void addTask(T task) {
        mEngine.addTask(task);
//        multiTask.wakeUp();
        wakeUp();
    }

    @Override
    protected void removeTask(int tag) {
        mEngine.removeTask(tag);
//        multiTask.wakeUp();
        wakeUp();
    }

    @Override
    protected void removeTask(T task) {
        mEngine.removeTask(task);
//        multiTask.wakeUp();
        wakeUp();
    }

    private class NetMultiTask {
        private List<ITaskContainer> taskContainerList;
        private HighPcEngine mEngine;

        public NetMultiTask(HighPcEngine engine) {
            mEngine = engine;
            int countThread = Runtime.getRuntime().availableProcessors() * 2;
            initThread(countThread);
        }

        public NetMultiTask(int countThread, HighPcEngine engine) {
            mEngine = engine;
            initThread(countThread);
        }

        private void initThread(int countThread) {
            taskContainerList = new ArrayList<>(countThread);
            for (int index = 0; index < countThread; index++) {
                ITaskContainer container = TaskExecutorPoolManager.getInstance().createJThread(mEngine);
                taskContainerList.add(container);
            }
        }

        public void wakeUp() {
            for (ITaskContainer container : taskContainerList) {
                ILoopTaskExecutor executor = container.getTaskExecutor();
                executor.resumeTask();
            }
        }


        public void startExec() {
            synchronized (NetMultiTask.class) {
                for (ITaskContainer container : taskContainerList) {
                    container.getTaskExecutor().startTask();
                }
            }
        }

        public void stopExec() {
            synchronized (NetMultiTask.class) {
                for (ITaskContainer container : taskContainerList) {
                    TaskExecutorPoolManager.getInstance().destroy(container);
                }
                taskContainerList.clear();
            }
        }
    }
}
