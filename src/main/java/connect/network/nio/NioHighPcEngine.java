package connect.network.nio;

import task.executor.ConsumerListAttribute;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ITaskContainer;

import java.nio.channels.SelectionKey;
import java.util.Iterator;

/**
 * 多线程engine
 *
 * @param <T>
 */
public class NioHighPcEngine<T extends BaseNioNetTask> extends NioEngine {

    private ITaskContainer mMainTaskContainer;
    private ITaskContainer mOtherTaskContainer;
    private IConsumerAttribute<SelectionKey> mAttribute;

    private long mMainEngineTag;

    public NioHighPcEngine(NioNetWork<T> work) {
        super(work);
    }


    @Override
    protected boolean isEngineRunning() {
        return mMainTaskContainer != null && mMainTaskContainer.getTaskExecutor().isLoopState();
    }

    @Override
    protected void onEngineRun() {
        //如果是主引擎处理事件分发
        if (Thread.currentThread().getId() == mMainEngineTag) {
            //检测是否有新的任务添加
            mWork.onCheckConnectTask();
            //检查是否有事件任务
            onEventDistribution();
            //清除要结束的任务
            mWork.onCheckRemoverTask();
        } else {
//                LogDog.d("==> 非主引擎");
            SelectionKey selectionKey = mAttribute.popCacheData();
            if (selectionKey != null) {
                mWork.onSelectionKey(selectionKey);
            } else {
                if (mOtherTaskContainer != null) {
                    mOtherTaskContainer.getTaskExecutor().pauseTask();
                }
            }
        }
    }

    private void onEventDistribution() {
        int count = 0;
        if (mWork.getConnectCache().isEmpty() && mWork.getDestroyCache().isEmpty()) {
            try {
                count = mWork.getSelector().select();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                count = mWork.getSelector().selectNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (count > 0) {
            Iterator<SelectionKey> iterator = mWork.getSelector().selectedKeys().iterator();
            if (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    //连接事件由主引擎处理
                    mWork.onSelectionKey(selectionKey);
                } else {
                    if (mOtherTaskContainer != null) {
                        mAttribute.pushToCache(selectionKey);
                        mOtherTaskContainer.getTaskExecutor().resumeTask();
                    }
                }
                iterator.remove();
            }
        }
    }

    @Override
    protected void startEngine() {
        if (mMainTaskContainer == null) {
            mMainTaskContainer = new TaskContainer(this);
            mMainEngineTag = mMainTaskContainer.getThread().getId();
            mMainTaskContainer.getTaskExecutor().startTask();
        }
        if (mOtherTaskContainer == null) {
            mOtherTaskContainer = new TaskContainer(this);
            mAttribute = new ConsumerListAttribute<>();
            mOtherTaskContainer.getTaskExecutor().setAttribute(mAttribute);
            mOtherTaskContainer.getTaskExecutor().startTask();
        }
    }

    @Override
    protected void stopEngine() {
        if (mMainTaskContainer != null) {
            mMainTaskContainer.getTaskExecutor().stopTask();
            mMainTaskContainer.release();
            mMainTaskContainer = null;
        }
        if (mOtherTaskContainer != null) {
            mOtherTaskContainer.getTaskExecutor().stopTask();
            mOtherTaskContainer.release();
            mOtherTaskContainer = null;
        }
        resumeEngine();
    }

}
