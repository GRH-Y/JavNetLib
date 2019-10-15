package connect.network.base;


public abstract class AbstractBioNetFactory<T extends BaseNetTask> extends AbstractNetFactory<T> {

    abstract protected boolean onConnectTask(T task);

    abstract protected void onExecRead(T task);

    abstract protected void onExecWrite(T task);

    abstract protected void onDisconnectTask(T task);

    protected AbstractBioNetFactory() {
        setEngine(new BioEngine(this));
    }

}
