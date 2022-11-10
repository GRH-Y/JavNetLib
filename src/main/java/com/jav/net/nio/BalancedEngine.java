package com.jav.net.nio;

import com.jav.net.entity.FactoryContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 高性能engine,对connect,read,write和disConnect事件分离
 *
 * @author yyz
 */
public class BalancedEngine extends NioNetEngine {

    protected List<NioNetEngine> mEngineList;

    protected NioNetEngine mClearEngine;

    public BalancedEngine(FactoryContext context) {
        super(context);
        setWorkStep((byte) (CREATE | SELECT));
    }

    @Override
    protected void onInitTask() {
        super.onInitTask();
        // 新建独立的 NioEngine 绑定  NioClearWork
        NioBalancedNetWork mainNetWork = mFactoryContext.getNetWork();
        NioClearWork clearWork = mainNetWork.getClearWork();
        FactoryContext clearContext = clearWork.getFactoryContext();
        mClearEngine = new NioNetEngine(clearContext);
        clearContext.setNetEngine(mClearEngine);
        // 只负责断开链接任务
        mClearEngine.setWorkStep(NioNetEngine.DESTROY);
        mClearEngine.startEngine();

        List<NioReadWriteNetWork> subNetWorkList = mainNetWork.getSubNetWorkList();
        if (subNetWorkList != null) {
            mEngineList = new ArrayList<>(subNetWorkList.size());
            for (NioReadWriteNetWork subNetWork : subNetWorkList) {
                FactoryContext context = subNetWork.getFactoryContext();
                NioNetEngine netEngine = new NioNetEngine(context);
                // 绑定net engine
                context.setNetEngine(netEngine);
                // 只负责读写的任务
                netEngine.setWorkStep(SELECT);
                netEngine.startEngine();
                mEngineList.add(netEngine);
            }
        }
    }

    @Override
    protected void onDestroyTask() {
        super.onDestroyTask();
        if (mClearEngine != null) {
            mClearEngine.stopEngine();
        }
    }


    @Override
    protected void stopEngine() {
        super.stopEngine();
        if (mEngineList != null) {
            for (Iterator<NioNetEngine> iterator = mEngineList.iterator(); iterator.hasNext(); ) {
                NioNetEngine engine = iterator.next();
                engine.stopEngine();
            }
        }
    }
}
