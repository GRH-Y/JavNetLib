package connect.network.base;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class BioEngine<T extends BaseNetTask> extends LowPcEngine {

    protected AbstractBioFactory<T> mFactory;

    /**
     * 正在执行任务的队列
     */
    protected Queue<T> mExecutorQueue;


    public BioEngine(AbstractBioFactory<T> factory) {
        mExecutorQueue = new ConcurrentLinkedQueue<>();
        this.mFactory = factory;
    }


    protected void onRemoveNeedDestroyTask(boolean isRemoveAll) {
        //销毁链接
        while (!mFactory.mDestroyCache.isEmpty()) {
            T task = mFactory.mDestroyCache.remove();
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
        if (mExecutorQueue.isEmpty() && mFactory.mConnectCache.isEmpty() && mFactory.mDestroyCache.isEmpty() && mExecutor.getLoopState()) {
            mExecutor.waitTask(0);
        }
    }

    protected void onCreateData() {

        //检测是否有新的任务添加
        if (!mFactory.mConnectCache.isEmpty()) {
            T task = mFactory.mConnectCache.remove();
            try {
                boolean isConnect = mFactory.onConnectTask(task);
                if (isConnect) {
                    mExecutorQueue.add(task);
                } else {
                    mFactory.mDestroyCache.add(task);
                }
            } catch (Exception e) {
                mFactory.mDestroyCache.add(task);
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
        mFactory.mConnectCache.clear();
        mExecutorQueue.clear();
        mFactory.mDestroyCache.clear();
    }

//    @Override
//    protected void removeTask(int tag) {
//        for (BaseNetTask task : mFactory.mDestroyCache) {
//            if (task.getTag() == tag) {
//                return;
//            }
//        }
//        for (BaseNetTask task : mExecutorQueue) {
//            if (task.getTag() == tag) {
//                mFactory.mDestroyCache.add((T) task);
//                break;
//            }
//        }
//    }

}
