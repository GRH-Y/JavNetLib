package connect.network.base;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class BioEngine<T extends BaseNetTask> extends LowPcEngine<T> {

    protected AbstractBioFactory mFactory;

    /**
     * 正在执行任务的队列
     */
    protected Queue<T> mExecutorQueue;


    public BioEngine(AbstractBioFactory factory) {
        mExecutorQueue = new ConcurrentLinkedQueue<>();
        this.mFactory = factory;
    }


    protected void onRemoveNeedDestroyTask(boolean isRemoveAll) {
        //销毁链接
        while (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            try {
                mFactory.onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mExecutorQueue.remove(task);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    @Override
    protected void onRunLoopTask() {
        onCreateData();
        onProcess();
        if (mExecutorQueue.isEmpty() && mConnectCache.isEmpty() && mDestroyCache.isEmpty() && mExecutor.getLoopState()) {
            mExecutor.waitTask(0);
        }
    }

    protected void onCreateData() {

        //检测是否有新的任务添加
        if (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            try {
                boolean isConnect = mFactory.onConnectTask(task);
                if (isConnect) {
                    mExecutorQueue.add(task);
                } else {
                    mDestroyCache.add(task);
                }
            } catch (Exception e) {
                mDestroyCache.add(task);
                e.printStackTrace();
            }
        }
    }

    protected void onProcess() {
        for (T task : mExecutorQueue) {
            if (mExecutor.getLoopState()) {
                try {
                    //执行读任务
                    mFactory.onExecRead(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    //执行写任务
                    mFactory.onExecWrite(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        //清除要结束的任务
        onRemoveNeedDestroyTask(false);
    }


    @Override
    protected void onDestroyTask() {
        //清除要结束的任务
        onRemoveNeedDestroyTask(true);
        //主动结束所有的任务
        while (!mExecutorQueue.isEmpty()) {
            T task = mExecutorQueue.remove();
            try {
                mFactory.onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mConnectCache.clear();
        mExecutorQueue.clear();
        mDestroyCache.clear();
    }

    @Override
    protected void removeTask(int tag) {
        for (BaseNetTask task : mDestroyCache) {
            if (task.getTag() == tag) {
                return;
            }
        }
        for (BaseNetTask task : mExecutorQueue) {
            if (task.getTag() == tag) {
                mDestroyCache.add((T) task);
                break;
            }
        }
    }

    @Override
    public void openHighPer() {

    }


}
