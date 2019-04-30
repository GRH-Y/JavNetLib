package connect.network.base;

import task.executor.BaseLoopTask;

public class FactoryEngine<T extends BaseNetTask> extends BaseLoopTask {


    @Override
    protected void onRunLoopTask() {
    }

    protected void addTask(T task) {
    }

    protected void removeTask(T task) {
    }

    protected void removeTask(int tag) {
    }

    protected void openHighPer() {
    }

    protected void startEngine() {
    }

    protected void stopEngine() {
    }
}
