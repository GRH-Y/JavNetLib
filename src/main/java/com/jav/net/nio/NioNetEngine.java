package com.jav.net.nio;

import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.FactoryContext;

/**
 * nio网络引擎,执行net work
 *
 * @author yyz
 */
public class NioNetEngine extends BaseNetEngine {

    /**
     * 处理select的事件
     */
    public static final byte SELECT = 2;


    public NioNetEngine(FactoryContext context) {
        super(context);
        mWorkStep = (byte) (CREATE | SELECT | DESTROY);
    }


    @Override
    protected void onCreateStepExt() {
        if ((mWorkStep & SELECT) == SELECT) {
            // 检查是否有读写任务
            AbsNioNetWork netWork = mFactoryContext.getNetWork();
            netWork.onSelectEvent();
        }
    }

    @Override
    public void resumeEngine() {
        super.resumeEngine();
        if (mFactoryContext != null) {
            BaseNetWork netWork = mFactoryContext.getNetWork();
            if (netWork instanceof AbsNioNetWork<?, ?>) {
                AbsNioNetWork nioNetWork = (AbsNioNetWork) netWork;
                if (nioNetWork.getSelector() != null) {
                    nioNetWork.getSelector().wakeup();
                }
            }
        }
    }


    @Override
    public void stopEngine() {
        super.stopEngine();
        resumeEngine();
    }
}
