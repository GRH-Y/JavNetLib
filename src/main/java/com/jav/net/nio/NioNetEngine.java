package com.jav.net.nio;

import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.BaseNetWork;

import java.util.ArrayList;
import java.util.List;

/**
 * nio网络引擎,执行net work
 *
 * @author yyz
 */
public class NioNetEngine extends BaseNetEngine {

    private List<BaseNetEngine> mEngineList;

    public NioNetEngine(BaseNetWork work) {
        super(work);
    }


    public NioNetEngine(BaseNetWork work, int workCount) {
        super(work);
        if (workCount <= 1) {
            return;
        }
        mEngineList = new ArrayList<>(workCount);
        for (int count = 1; count < workCount; count++) {
            BaseNetEngine engine = new BaseNetEngine(work);
            mEngineList.add(engine);
        }
    }

    @Override
    public void startEngine() {
        super.startEngine();
        if (mEngineList != null) {
            for (BaseNetEngine engine : mEngineList) {
                engine.startEngine();
            }
        }
    }

    @Override
    public void stopEngine() {
        super.stopEngine();
        if (mEngineList != null) {
            for (BaseNetEngine engine : mEngineList) {
                engine.stopEngine();
            }
        }
    }
}
