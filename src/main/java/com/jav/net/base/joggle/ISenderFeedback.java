package com.jav.net.base.joggle;

public interface ISenderFeedback {

    /**
     * 发送数据回调反馈
     *
     * @param sender
     * @param data
     */
    void onSenderFeedBack(INetSender sender, Object data, Throwable e);
}
