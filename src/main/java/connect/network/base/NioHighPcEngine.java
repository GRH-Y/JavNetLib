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
public class NioHighPcEngine<T extends BaseNetTask> extends NioEngine<T> {

//    private NetMultiTask multiTask = null;

    private List<ITaskContainer> taskContainerList;

    private int threadCount = 4;

    public NioHighPcEngine() {
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    protected void startEngine() {
        if (taskContainerList == null) {
            taskContainerList = new ArrayList<>(threadCount);
        }
        if (taskContainerList.isEmpty()) {
            for (int count = 0; count < threadCount; count++) {
                ITaskContainer container = TaskExecutorPoolManager.getInstance().createJThread(this);
                container.getTaskExecutor().startTask();
                taskContainerList.add(container);
            }
        }
//        if (multiTask == null) {
//            multiTask = new NetMultiTask(threadCount, this);
//        }
//        multiTask.startExec();
    }

    @Override
    protected void stopEngine() {
        if (taskContainerList != null) {
            for (ITaskContainer container : taskContainerList) {
                container.getTaskExecutor().blockStopTask();
            }
            taskContainerList.clear();
        }
//        if (multiTask != null) {
//            multiTask.stopExec();
//        }
    }

    private void wakeUp() {
        for (ITaskContainer container : taskContainerList) {
            container.getTaskExecutor().resumeTask();
        }
    }

//    @Override
//    protected void addTask(T task) {
//        mEngine.addTask(task);
////        multiTask.wakeUp();
//        wakeUp();
//    }

    @Override
    protected void removeTask(int tag) {
        removeTaskImp(tag);
        wakeUp();
//        multiTask.wakeUp();
    }

//    @Override
//    protected void removeTask(T task) {
//        mEngine.removeTask(task);
////        multiTask.wakeUp();
//        wakeUp();
//    }

    private class NetMultiTask {
        private List<ITaskContainer> taskContainerList;
        private NioHighPcEngine mEngine;

        public NetMultiTask(NioHighPcEngine engine) {
            mEngine = engine;
            int countThread = Runtime.getRuntime().availableProcessors() * 2;
            initThread(countThread);
        }

        public NetMultiTask(int countThread, NioHighPcEngine engine) {
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
