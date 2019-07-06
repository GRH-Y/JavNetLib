package connect.network.base;

import task.executor.BaseLoopTask;

public abstract class PcEngine extends BaseLoopTask {

    @Override
    protected void onRunLoopTask() {
        //必须重写，不然该方法没有权限访问
    }

    protected boolean isRunning() {
        return false;
    }

    protected void resumeTask() {
    }

    protected void startEngine() {
    }

    protected void stopEngine() {
    }

}
