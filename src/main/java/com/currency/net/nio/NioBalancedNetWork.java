package com.currency.net.nio;

import com.currency.net.base.NetTaskComponent;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.entity.FactoryContext;
import com.currency.net.entity.NetTaskStatusCode;
import log.LogDog;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * NioBalancedNetWork 只处理连接请求的事件,把链接成功的channel分发到NioReadWriteWork来处理读写事件
 */
public class NioBalancedNetWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private List<NioReadWriteNetWork> mRWNetWorkList;

    private final int DEFAULT_WORK_COUNT = 3;

    private int mConfigWorkCount = DEFAULT_WORK_COUNT;

    private NioClearWork mClearWork;

    public NioBalancedNetWork(FactoryContext intent) {
        super(intent);
    }

    /**
     * 均衡分配的net work
     *
     * @param intent
     * @param workCount net work的数量
     */
    public NioBalancedNetWork(FactoryContext intent, int workCount) {
        super(intent);
        if (workCount <= 0) {
            workCount = DEFAULT_WORK_COUNT;
        }
        mConfigWorkCount = workCount;
    }

    @Override
    protected void init() {
        super.init();
        initRWNetWork(mConfigWorkCount);
    }

    private void initRWNetWork(int workCount) {
        FactoryContext clearContext = new FactoryContext();
        //跟NioBalancedNetWork共享同个NetTaskContainer
        clearContext.setNetTaskContainer(mFactoryContext.getNetTaskContainer());
        mClearWork = new NioClearWork(clearContext);
        clearContext.setNetWork(mClearWork);

        mRWNetWorkList = new ArrayList<>(workCount);
        for (int index = 0; index < workCount; index++) {
            FactoryContext rwContext = new FactoryContext();
            rwContext.setSSLFactory(mFactoryContext.getSSLFactory());
            rwContext.setNetTaskContainer(new NetTaskComponent(rwContext));
            NioReadWriteNetWork netWork = new NioReadWriteNetWork<>(rwContext, mClearWork);
            netWork.init();
            rwContext.setNetWork(netWork);
            mRWNetWorkList.add(netWork);
            //绑定当前的NioReadWriteNetWork 让ClearWork 来处理addUnExecTask
            mClearWork.bindRWNetWork(netWork);
        }
    }

    public List<NioReadWriteNetWork> getSubNetWorkList() {
        return mRWNetWorkList;
    }

    public NioClearWork getClearWork() {
        return mClearWork;
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) {
        try {
            if (channel.isConnected()) {
                //改变channel注册到其它的Selector
                changeSelectReg(netTask);
            } else {
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, netTask);
                netTask.setSelectionKey(selectionKey);
            }
        } catch (Throwable e) {
            LogDog.e("## channel register error , url = " + netTask.getHost() + " port = " + netTask.getPort());
            callBackInitStatusChannelError(netTask, e);
        }
    }

    @Override
    protected void onConnectEvent(SelectionKey key) {
        C channel = (C) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        try {
            boolean isConnect = channel.finishConnect();
            if (isConnect) {
                //改变channel注册到其它的Selector
                changeSelectReg(netTask);
            } else {
                callBackInitStatusChannelError(netTask, null);
            }
        } catch (Throwable e) {
            callBackInitStatusChannelError(netTask, e);
        }
    }

    private void changeSelectReg(T netTask) {
        if (netTask.updateTaskStatus(NetTaskStatusCode.RUN, NetTaskStatusCode.NONE)) {
            NioReadWriteNetWork netWork = findOptimalNetWork();
            netWork.registerReadWriteEvent(netTask);
        } else {
            //更新ASSIGN状态失败则结束task
            LogDog.w("## changeSelectReg update status to ASSIGN fails !!!");
            FactoryContext context = mClearWork.getFactoryContext();
            INetTaskContainer taskContainer = context.getNetTaskContainer();
            taskContainer.addUnExecTask(netTask);
            NioNetEngine netEngine = context.getNetEngine();
            netEngine.resumeEngine();
        }
    }

    private NioReadWriteNetWork findOptimalNetWork() {
        NioReadWriteNetWork target = null;
        for (NioReadWriteNetWork netWork : mRWNetWorkList) {
            if (target == null) {
                target = netWork;
            } else {
                if (target.getPressureValue() > netWork.getPressureValue()) {
                    target = netWork;
                }
            }
        }
        return target;
    }


}
