package com.currency.net.nio;

import com.currency.net.entity.FactoryContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 高性能的engine
 */
public class NioBalancedServerEngine extends NioNetEngine {

    private List<NioNetEngine> mEngineList;

    /**
     * 高性能的engine
     *
     * @param context 集成环境所需的对象
     */
    public NioBalancedServerEngine(FactoryContext context) {
        super(context);
        setWorkStep(ACCEPT);
    }


    @Override
    protected void onInitTask() {
        super.onInitTask();
        NioBalancedNetWork netWork = mFactoryContext.getNetWork();
        List<NioReadWriteNetWork> subNetWorkList = netWork.getSubNetWorkList();
        if (subNetWorkList != null) {
            mEngineList = new ArrayList<>(subNetWorkList.size());
            for (NioReadWriteNetWork subNetWork : subNetWorkList) {
                FactoryContext context = subNetWork.getFactoryContext();
                NioNetEngine nioEngine = new NioNetEngine(context);
                //只负责读写的任务
                nioEngine.setWorkStep((byte) (NioNetEngine.CONNECT | NioNetEngine.RW));
                nioEngine.startEngine();
                mEngineList.add(nioEngine);
            }
        }
    }

    @Override
    protected void stopEngine() {
        super.stopEngine();
        if (mEngineList != null) {
            for (NioNetEngine engine : mEngineList) {
                engine.stopEngine();
            }
        }
    }
}
