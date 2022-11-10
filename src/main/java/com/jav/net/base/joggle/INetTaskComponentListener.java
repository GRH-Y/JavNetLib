package com.jav.net.base.joggle;

/**
 * 网络任务组件监听器,监听任务的添加
 *
 * @author yyz
 */
public interface INetTaskComponentListener {

    /**
     * 添加任务回调
     *
     * @param isExecTask true是执行任务，false是销毁任务
     */
    void onAppendChange(boolean isExecTask);
}
