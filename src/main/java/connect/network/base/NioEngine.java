package connect.network.base;

public class NioEngine<T extends BaseNioNetTask> extends LowPcEngine {

    protected AbstractNioNetFactory<T> mFactory;

    protected NioEngine() {
    }

    protected NioEngine(AbstractNioNetFactory<T> factory) {
        this.mFactory = factory;
    }

    protected void setFactory(AbstractNioNetFactory<T> mFactory) {
        this.mFactory = mFactory;
    }

    @Override
    protected void onRunLoopTask() {
        //检测是否有新的任务添加
        mFactory.onCheckConnectTask(false);
        //检查是否有读写任务
        mFactory.onSelectorTask();
        //清除要结束的任务
        mFactory.onCheckRemoverTask(false);
    }


    @Override
    protected void onDestroyTask() {
        mFactory.destroyTaskImp();
    }

}
