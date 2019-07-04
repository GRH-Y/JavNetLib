package connect.network.base;

public abstract class AbstractBioFactory<T extends BaseNetTask> extends AbstractFactory<T> {

    abstract protected boolean onConnectTask(T task);

    abstract protected void onExecRead(T task);

    abstract protected void onExecWrite(T task);

    abstract protected void onDisconnectTask(T task);

    protected AbstractBioFactory() {
        setEngine(new BioEngine(this));
    }
}
