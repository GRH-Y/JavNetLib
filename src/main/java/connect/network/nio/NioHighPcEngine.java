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

    private ITaskContainer mainTaskContainer;
    private ITaskContainer otherTaskContainer;
    private IConsumerAttribute<SelectionKey> attribute;

    private long mainEngineTag;

    public NioHighPcEngine(NioNetWork<T> work) {
        super(work);
    }


    @Override
    protected boolean isEngineRunning() {
        return mainTaskContainer != null && mainTaskContainer.getTaskExecutor().getLoopState();
    }

    @Override
    protected void onEngineRun() {
        //如果是主引擎处理事件分发
        if (Thread.currentThread().getId() == mainEngineTag) {
            //检测是否有新的任务添加
            mWork.onCheckConnectTask();
            //检查是否有事件任务
            onEventDistribution();
            //清除要结束的任务
            mWork.onCheckRemoverTask();
        } else {
//                LogDog.d("==> 非主引擎");
            SelectionKey selectionKey = attribute.popCacheData();
            if (selectionKey != null) {
                mWork.onSelectionKey(selectionKey);
            } else {
                if (otherTaskContainer != null) {
                    otherTaskContainer.getTaskExecutor().pauseTask();
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
                    if (otherTaskContainer != null) {
                        attribute.pushToCache(selectionKey);
                        otherTaskContainer.getTaskExecutor().resumeTask();
                    }
                }
                iterator.remove();
            }
        }
    }

    @Override
    protected void startEngine() {
        if (mainTaskContainer == null) {
            mainTaskContainer = new TaskContainer(this);
            mainEngineTag = mainTaskContainer.getThread().getId();
            mainTaskContainer.getTaskExecutor().startTask();
        }
        if (otherTaskContainer == null) {
            otherTaskContainer = new TaskContainer(this);
            attribute = new ConsumerListAttribute<>();
            otherTaskContainer.getTaskExecutor().setAttribute(attribute);
            otherTaskContainer.getTaskExecutor().startTask();
        }
    }

    @Override
    protected void stopEngine() {
        if (mainTaskContainer != null) {
            mainTaskContainer.getTaskExecutor().stopTask();
            mainTaskContainer.release();
            mainTaskContainer = null;
        }
        if (otherTaskContainer != null) {
            otherTaskContainer.getTaskExecutor().stopTask();
            otherTaskContainer.release();
            otherTaskContainer = null;
        }
        resumeEngine();
    }

}
