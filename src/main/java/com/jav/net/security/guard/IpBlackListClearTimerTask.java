package com.jav.net.security.guard;


import com.jav.common.log.LogDog;
import com.jav.common.storage.FileHelper;
import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.LoopTaskExecutor;
import com.jav.thread.executor.TaskContainer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ip黑名单定时清除任务，凌晨执行
 *
 * @author yyz
 */
public class IpBlackListClearTimerTask extends LoopTask {

    private TaskContainer mContainer;
    private String mOutFilePath;
    private static final long s_OneDay = 24 * 60 * 60 * 1000;
    private boolean isFirst = true;


    public void configOutLogPath(String path) {
        mOutFilePath = path;
    }

    @Override
    protected void onInitTask() {
        LogDog.d("start reset black list task !");
    }

    @Override
    protected void onRunLoopTask() {
        LoopTaskExecutor executor = mContainer.getTaskExecutor();
        if (isFirst) {
            executor.waitTask(getMilliSecondsNextEarlyMorning());
            isFirst = false;
        } else {
            executor.waitTask(s_OneDay);
        }
        LogDog.d("exec reset black list !!!");
        if (executor.isLoopState()) {
            // 到凌晨开始清楚ip黑名单
            if (mOutFilePath != null) {
                byte[] allData = IpBlackListManager.getInstance().getBlackList();
                if (allData == null) {
                    return;
                }
                String date = getDateTimeFromMilli(System.currentTimeMillis());
                File ipLogFile = FileHelper.crateFile(mOutFilePath, date + ".ip");
                FileHelper.writeFile(ipLogFile, allData);
                LogDog.w("exec save black list to file = " + ipLogFile.getAbsolutePath());
            }
            IpBlackListManager.getInstance().clearBlackList();
        }
        LogDog.d("stop reset black list task !");
    }

    public void start() {
        if (mContainer == null) {
            mContainer = new TaskContainer(this);
            LoopTaskExecutor executor = mContainer.getTaskExecutor();
            executor.startTask();
        }
    }

    public void stop() {
        if (mContainer != null) {
            LoopTaskExecutor executor = mContainer.getTaskExecutor();
            executor.stopTask();
        }
    }


    private String getDateTimeFromMilli(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 获取当前时间到凌晨12点的毫秒
     *
     * @return
     */
    public Long getMilliSecondsNextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis());
    }
}
