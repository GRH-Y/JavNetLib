package com.currency.net.nio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.entity.FactoryContext;

public class NioNetEngine extends AbsNetEngine {

    public static final byte CONNECT = 1;
    public static final byte RW = 2;
    public static final byte DISCONNECT = 4;
    public static final byte ACCEPT = 8;

    private byte mWorkStep = (byte) (CONNECT | RW | DISCONNECT);


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
        if ((mWorkStep & CONNECT) == CONNECT) {
            //检测是否有新的任务添加
            netWork.onCheckConnectTask();
        }
        if ((mWorkStep & RW) == RW) {
            //检查是否有读写任务
            netWork.onRWDataTask();
        }
        if ((mWorkStep & DISCONNECT) == DISCONNECT) {
            //清除要结束的任务
            netWork.onCheckRemoverTask();
        }
    }

    @Override
    protected void pauseEngine() {
        super.pauseEngine();
    }

    @Override
    protected void resumeEngine() {
        AbsNioNetWork netWork = mFactoryContext.getNetWork();
        if (netWork.getSelector() != null) {
            netWork.getSelector().wakeup();
        }
        super.resumeEngine();
    }

    @Override
    protected void onDestroyTask() {
        AbsNioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onRecoveryTaskAll();
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
