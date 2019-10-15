package connect.network.tcp;

import connect.network.base.AbstractBioNetFactory;
import connect.network.base.BaseNetTask;

public abstract class AbstractServerNetFactory<T extends BaseNetTask> extends AbstractBioNetFactory<T> {

    abstract protected void onAcceptTask(T task);

    @Override
    protected void onExecRead(T task) {
        onAcceptTask(task);
    }

    @Override
    protected void onExecWrite(T task) {
        onAcceptTask(task);
    }

}
