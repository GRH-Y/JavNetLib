package connect.network.tcp;

import connect.network.base.AbstractBioFactory;
import connect.network.base.BaseNetTask;

public abstract class AbstractServerFactory<T extends BaseNetTask> extends AbstractBioFactory<T> {

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
