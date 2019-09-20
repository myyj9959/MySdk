package com.myyj.sdk.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.widget.Toast;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.tools.sercer2.ServerHelper2;

public class LogHelper extends BaseHelper {
    final static String TAG = "YOSHI_LOG";


    public static void d(String msg) {
        if (MySDK.getInstance().getTestDevice())
            Log.d(TAG, msg);
    }

    public static void i(String msg) {
        if (MySDK.getInstance().getTestDevice())
            Log.i(TAG, msg);
    }

    public static void w(String msg) {
        if (MySDK.getInstance().getTestDevice())
            Log.e(TAG, msg);
    }

    public static void e(Throwable e) {
        Log.e(TAG, "", e);
    }

    public static void toast(final String msg) {
        if (MySDK.getInstance().getTestDevice())
            LogHelper.w("toast: " + msg);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void alert(final String title, final String msg, final String btnTitle,
                             final DialogInterface.OnDismissListener onDismiss) {
        if (MySDK.getInstance().getTestDevice()) {
            LogHelper.w("toast: " + msg);
            LogHelper.d("LogHelper.alert " + title + " " + msg);

        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setMessage(msg)
                        .setOnDismissListener(onDismiss)
                        .setPositiveButton(btnTitle, null)
                        .show();
            }
        });
    }

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isInDebug() {
        try {
            ApplicationInfo info = getActivity().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
