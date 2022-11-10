package com.jav.net.nio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.entity.FactoryContext;

/**
 * nio网络引擎,执行net work
 *
 * @author yyz
 */
public class NioNetEngine extends AbsNetEngine {

    /**
     * 创建任务
     */
    public static final byte CREATE = 1;
    /**
     * 处理select的事件
     */
    public static final byte SELECT = 2;
    /**
     * 销毁任务
     */
    public static final byte DESTROY = 4;

    private byte mWorkStep = (byte) (CREATE | SELECT | DESTROY);


    public NioNetEngine(FactoryContext context) {
        super(context);
    }

    public void setWorkStep(byte process) {
        this.mWorkStep = process;
    }


    @Override
    protected void onInitTask() {
        AbsNioNetWork netWork = mFactoryContext.getNetWork();
        netWork.init();
    }

    @Override
    protected void onEngineRun() {
        AbsNioNetWork netWork = mFactoryContext.getNetWork();
        if ((mWorkStep & CREATE) == CREATE) {
            // 检测是否有新的任务添加
            netWork.onCreateTask();
        }
        if ((mWorkStep & SELECT) == SELECT) {
            // 检查是否有读写任务
            netWork.onSelectEvent();
        }
        if ((mWorkStep & DESTROY) == DESTROY) {
            // 清除要结束的任务
            netWork.onDestroyTask();
        }
    }

    @Override
    protected void pauseEngine() {
        super.pauseEngine();
    }

    @Override
    protected void resumeEngine() {
        super.resumeEngine();
        if (mFactoryContext != null) {
            AbsNioNetWork netWork = mFactoryContext.getNetWork();
            if (netWork.getSelector() != null) {
                netWork.getSelector().wakeup();
            }
        }
    }

    @Override
    protected void onDestroyTask() {
        AbsNioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onDestroyTaskAll();
        release();
    }

    @Override
    protected void startEngine() {
        super.startEngine();
    }

    @Override
    protected void stopEngine() {
        super.stopEngine();
        resumeEngine();
    }
}
