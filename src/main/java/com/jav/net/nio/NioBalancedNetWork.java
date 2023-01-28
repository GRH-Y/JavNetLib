package com.jav.net.nio;

import com.jav.net.base.NetTaskComponent;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NioBalancedNetWork 只处理连接请求的事件,把链接成功的channel分发到NioReadWriteWork来处理读写事件
 *
 * @author yyz
 */
public class NioBalancedNetWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private List<NioReadWriteNetWork> mRWNetWorkList;


    private final int DEFAULT_WORK_COUNT = 3;

    private int mConfigWorkCount = DEFAULT_WORK_COUNT;

    private NioClearWork mClearWork;


    public NioBalancedNetWork(FactoryContext context) {
        super(context);
    }

    /**
     * 均衡分配的net work
     *
     * @param context
     * @param workCount net work的数量
     */
    public NioBalancedNetWork(FactoryContext context, int workCount) {
        super(context);
        if (workCount <= 0) {
            workCount = DEFAULT_WORK_COUNT;
        }
        mConfigWorkCount = workCount;
    }

    public List<NioReadWriteNetWork> getSubNetWorkList() {
        return mRWNetWorkList;
    }

    public NioClearWork getClearWork() {
        return mClearWork;
    }

    @Override
    protected void init() {
        super.init();
        initRWNetWork(mConfigWorkCount);
    }

    @Override
    protected void onSelectEvent() {
        int count = 0;
        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
        if (taskFactory.isConnectQueueEmpty()) {
            try {
                count = mSelector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                count = mSelector.selectNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                try {
                    onSelectionKey(selectionKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initRWNetWork(int workCount) {
        // 设置监听主Component，用于唤醒clear engine处理移除task
        FactoryContext mainContext = getFactoryContext();
        NetTaskComponent mainComponent = mainContext.getNetTaskComponent();

        FactoryContext clearContext = new FactoryContext();
        clearContext.setNetTaskComponent(mainComponent);
        mClearWork = new NioClearWork(clearContext);
        clearContext.setNetWork(mClearWork);
        mainComponent.setDestroyFactoryContext(clearContext);

        mRWNetWorkList = new ArrayList<>(workCount);
        for (int index = 0; index < workCount; index++) {
            FactoryContext rwContext = new FactoryContext();
            rwContext.setNetTaskComponent(mainComponent);
            rwContext.setSSLFactory(mainContext.getSSLFactory());
            NioReadWriteNetWork netWork = new NioReadWriteNetWork<>(rwContext);
            rwContext.setNetWork(netWork);
            netWork.init();
            mRWNetWorkList.add(netWork);
        }
    }


    @Override
    protected void registerEvent(T netTask, C channel) {
        SelectionKey selectionKey = netTask.getSelectionKey();
        if (selectionKey != null) {
            // 解绑当前的selector,取消通道注册
            selectionKey.cancel();
        }
        NioReadWriteNetWork netWork = findOptimalNetWork();
        netWork.registerReadWriteEvent(netTask);
    }

    /**
     * 匹配最优的work，根据work当前的task数量选最少的
     *
     * @return
     */
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
