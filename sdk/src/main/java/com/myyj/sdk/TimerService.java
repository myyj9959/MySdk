package com.myyj.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.myyj.sdk.tools.LogHelper;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    Timer timer;  //定时器

    TimerTask task;//定时执行的任务

    @Override

    public IBinder onBind(Intent intent) {

        return null;

    }

    private int i = 0;

    @Override

    public void onCreate() {

        timer = new Timer();

        task = new TimerTask() {

            @Override

            public void run() {

                try {

                    Thread.sleep(1000);
                    i++;
                    LogHelper.d("执行了一次定时任务");
                    msdk.dispatchEvent("TimerService", "心脏跳动"+i);
                    if (i == 40) {
                        LogHelper.d("停止了定时任务");
                        msdk.dispatchEvent("TimerService", "心脏停止跳动");
                        timer.cancel();

                        task.cancel();
                    }

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

            }

        };

        // 参数说明：1、任务2、延迟指定的时间后开始执行3、以指定的频率重复执行任务

        timer.scheduleAtFixedRate(task, 1000, 15000);
        super.onCreate();

    }

    @Override

    public void onDestroy() {

        LogHelper.d("停止了定时任务");
        msdk.dispatchEvent("TimerService", "心脏停止跳动");
        timer.cancel();

        task.cancel();

        super.onDestroy();
    }

}
