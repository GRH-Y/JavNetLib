package connect.network.base;

public class NioEngine<T extends BaseNetTask> extends LowPcEngine {

    protected AbstractNioFactory<T> mFactory;

    protected NioEngine() {
    }

    protected NioEngine(AbstractNioFactory<T> factory) {
        this.mFactory = factory;
    }

    protected void setFactory(AbstractNioFactory<T> mFactory) {
        this.mFactory = mFactory;
    }

    @Override
    protected void onRunLoopTask() {
        //检测是否有新的任务添加
        mFactory.checkConnectTaskImp(false);
        //检查是否有读写任务
        mFactory.selectorTaskImp();
        //清除要结束的任务
        mFactory.checkRemoverTaskImp(false);
    }


    @Override
    protected void onDestroyTask() {
        mFactory.destroyTask();
    }

}
