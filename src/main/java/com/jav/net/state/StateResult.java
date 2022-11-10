package com.jav.net.state;

/**
 * StateResult 状态更新结果
 * @author yyz
 */
public final class StateResult {

    private boolean mUpdateValue = false;
    private boolean mUserValue = false;

    public boolean getUpdateValue() {
        return mUpdateValue;
    }

    public boolean getUserValue() {
        return mUserValue;
    }

    /**
     * 用户定义结果
     *
     * @param value
     */
    public void setUserValue(boolean value) {
        mUserValue = value;
    }

    /**
     * 真实的更新结果
     *
     * @param value
     */
    protected void setUpdateValue(boolean value) {
        mUpdateValue = value;
    }
}