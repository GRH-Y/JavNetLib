package com.currency.net.nio;

import com.currency.net.entity.FactoryContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 高性能的engine
 */
public class NioBalancedClientEngine extends NioNetEngine {

    private List<NioNetEngine> mEngineList;

    private NioNetEngine clearEngine;

    /**
     * 高性能的engine
     *
     * @param context 集成环境所需的对象
     */
    public NioBalancedClientEngine(FactoryContext context) {
        super(context);
        setWorkStep((byte) (CONNECT | RW));
    }


    @Override
    protected void onInitTask() {
        super.onInitTask();
        //新建独立的 NioEngine 绑定  NioClearWork
        NioBalancedNetWork mainNetWork = mFactoryContext.getNetWork();
        NioClearWork clearWork = mainNetWork.getClearWork();
        FactoryContext clearContext = clearWork.getFactoryContext();
        clearEngine = new NioNetEngine(clearWork.getFactoryContext());
        clearContext.setNetEngine(clearEngine);
        //只负责断开链接任务
        clearEngine.setWorkStep(NioNetEngine.DISCONNECT);
        clearEngine.startEngine();

        List<NioReadWriteNetWork> subNetWorkList = mainNetWork.getSubNetWorkList();
        if (subNetWorkList != null) {
            mEngineList = new ArrayList<>(subNetWorkList.size());
            for (NioReadWriteNetWork subNetWork : subNetWorkList) {
                FactoryContext context = subNetWork.getFactoryContext();
                NioNetEngine netEngine = new NioNetEngine(context);
                //绑定net engine
                context.setNetEngine(netEngine);
                //只负责读写的任务
                netEngine.setWorkStep((byte) (NioNetEngine.CONNECT | NioNetEngine.RW));
                netEngine.startEngine();
                mEngineList.add(netEngine);
            }
        }
    }

    @Override
    protected void resumeEngine() {
        super.resumeEngine();
        if (mEngineList != null) {
            for (Iterator<NioNetEngine> iterator = mEngineList.iterator(); iterator.hasNext(); ) {
                NioNetEngine engine = iterator.next();
                engine.resumeEngine();
            }
        }
        if (clearEngine != null) {
            clearEngine.resumeEngine();
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
        if (clearEngine != null) {
            clearEngine.stopEngine();
        }
    }
}
