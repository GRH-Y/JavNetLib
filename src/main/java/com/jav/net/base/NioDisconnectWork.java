package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 *
 * @author yyz
 */
public class NioDisconnectWork extends BaseNetWork {


    private BaseNetEngine mEngine;
    /**
     * 销毁任务队列
     */
    protected Queue<BaseNetTask> mDestroyCache;

    protected NioDisconnectWork(FactoryContext context) {
        super(context);
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }


    @Override
    protected void onWorkRun() {
        BaseNetTask netTask = mDestroyCache.poll();
        if (netTask == null) {
            mEngine.pauseEngine();
        } else {
            execDisconnectTask(netTask);
        }
    }

    @Override
    protected void onWorkEnd() {
        SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
        if (eventHubs != null) {
            List<SelectionKey> registerKeyList = eventHubs.getRegisterKey();
            if (registerKeyList != null) {
                // 线程准备结束，释放所有链接
                for (SelectionKey selectionKey : registerKeyList) {
                    BaseNetTask task = (BaseNetTask) selectionKey.attachment();
                    if (task != null) {
                        execDisconnectTask(task);
                    }
                }
            }
        }
        mDestroyCache.clear();

        //关闭selector
        SelectorEventHubs selectorCreator = mFactoryContext.getSelectorEventHubs();
        selectorCreator.destroy();
    }

    private void execDisconnectTask(BaseNetTask netTask) {
        // 等待task状态进入IDLING再执行回收
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getState() == NetTaskStatus.INVALID) {
            // 避免多次执行
            LogDog.w("## disconnect task state is  INVALID !!!");
            return;
        }
        disconnectChannel(netTask);
    }

    private void disconnectChannel(BaseNetTask netTask) {
        SelectionKey selectionKey = netTask.getSelectionKey();
        if (selectionKey != null) {
            selectionKey.cancel();
        }
        try {
            netTask.onCloseChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            NetworkChannel channel = netTask.getChannel();
            if (channel != null) {
                if (channel instanceof SocketChannel) {
                    SocketChannel socketChannel = (SocketChannel) channel;
                    if (socketChannel.isConnected()) {
                        try {
                            socketChannel.shutdownInput();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        try {
                            socketChannel.shutdownOutput();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    channel.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            netTask.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            netTask.setAddress(null, -1);
            netTask.setChannel(null);
            netTask.setSelectionKey(null);
        }
        for (IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
             !stateMachine.updateState(stateMachine.getState(), NetTaskStatus.INVALID); ) {
            LogDog.w("## for updateState task state is = " + stateMachine.getState());
        }
        LogDog.i("#clear# disconnect task complete, task = " + netTask);
    }


    //----------------------------------- public -------------------------------------------------

    public boolean isContains(BaseNetTask task) {
        return mDestroyCache.contains(task);
    }


    public boolean pushDisconnectTask(BaseNetTask task) {
        boolean ret = mDestroyCache.offer(task);
        if (mEngine != null) {
            mEngine.resumeEngine();
        }
        return ret;
    }

    public void startWork() {
        if (mEngine == null) {
            mEngine = new BaseNetEngine(this);
            mEngine.startEngine();
        }
    }

    public void stopWork() {
        if (mEngine != null) {
            mEngine.stopEngine();
        }
    }


}
