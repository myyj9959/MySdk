package com.myyj.gamebase;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        UMConfigure.setLogEnabled(true);
        // 初始化SDK
        UMConfigure.init(this, /*getAppKey(this), getChannelCode(this),*/ UMConfigure.DEVICE_TYPE_PHONE, null);
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        // 支持在子进程中统计自定义事件
        UMConfigure.setProcessEvent(true);

    }

    public static String getAppKey(Context context) {
        String code = getMetaData(context, "UMENG_APPKEY");
        if (code != null) {

            return code;
        }
        return "0";
    }

    public static String getChannelCode(Context context) {
        String code = getMetaData(context, "UMENG_CHANNEL");
        if (code != null) {
            return code;
        }
        return "C_000";
    }

    private static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
        }
        return null;
    }
}

