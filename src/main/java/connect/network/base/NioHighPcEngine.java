package connect.network.base;

import task.executor.TaskContainer;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ITaskContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * 多线程engine
 *
 * @param <T>
 */
public class NioHighPcEngine<T extends BaseNetTask> extends NioEngine<T> {

    private List<ITaskContainer> taskContainerList;

    private int threadCount = 4;

    public NioHighPcEngine() {
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    protected boolean isRunning() {
        if (taskContainerList.isEmpty()) {
            return false;
        }
        ITaskContainer container = taskContainerList.get(0);
        return container.getTaskExecutor().getLoopState();
    }

    @Override
    protected void startEngine() {
        if (taskContainerList == null) {
            taskContainerList = new ArrayList<>(threadCount);
        }
        if (taskContainerList.isEmpty()) {
            for (int count = 0; count < threadCount; count++) {
                ITaskContainer container = new TaskContainer(this);
                container.getTaskExecutor().startTask();
                taskContainerList.add(container);
            }
        }
    }

    @Override
    protected void stopEngine() {
        if (taskContainerList != null) {
            for (ITaskContainer container : taskContainerList) {
                container.getTaskExecutor().blockStopTask();
                TaskExecutorPoolManager.getInstance().destroy(container);
            }
            taskContainerList.clear();
        }
    }

}
