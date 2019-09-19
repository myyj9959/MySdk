package com.myyj.gamebase;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.myyj.gamebase.udid.OpenUDID;
import com.myyj.gamebase.utils.ValidatorUtils;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.MySDKSimple;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.msdk;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.utils.UMUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public static MainActivity sInstance;
    private Context mContext;
    public static Handler handlerLogin, handlerBind, handlerSubmitId;
    private LoginDialog loginActivity;
    private BindDialog bindActivity;

    private SMSBroadcastReceiver mSMSBroadcastReceiver;

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public EditText editVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏显示窗口
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
//        checkAndRequestPermissions();
        mainActivity = this;
        sInstance = this;
        mContext = this;

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setSessionContinueMillis(1000 * 40);
        SmsContent content = new SmsContent(new Handler());
        //注册短信变化监听
        this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, content);

        String channelId = MyApplication.getChannelCode(this);
//        String udid = OpenUDID.GetOpenUDID(this);

        SharedPreferences sp = getSharedPreferences("uuid", 0);
        String udid = sp.getString("udid", null);//UserData.getInstance().getUDID();
        if (null == udid || udid.length() <= 0) {
            udid = OpenUDID.GetOpenUDID(this);
            if (null == udid || udid.length() <= 0) {
                udid = UMUtils.getUMId(this);
            }
            if (null == udid || udid.length() <= 0) {
                udid = "random_" + Math.abs(new Random().nextLong());
            }
//            else {
//                udid = udid + "_random_" + Math.abs(new Random().nextLong());
//            }
//            UserData.getInstance().setUDID(udid);
            sp.edit().putString("udid", udid).commit();
        }
//
//        MySDK.getInstance().init("sdktest", 0, channelId, udid,"");

        msdk.loginServer(new ResultCallback() {
            @Override
            public void callback(int state, String result) {

            }
        });

        handlerLogin = new Handler() {
            public void handleMessage(Message msg) {
                activityLoginDialog();
            }
        };

        handlerBind = new Handler() {
            public void handleMessage(Message msg) {
                activityBindDialog();
            }
        };

        handlerSubmitId = new Handler() {
            public void handleMessage(Message msg) {
                activitySubmitIdDialog();
            }
        };
        activityLoginDialog();
//        activitySubmitIdDialog();
//        checkAndRequestPermission();
//        SuperSmsManager.readAllSms();

    }

//    private void checkAndRequestPermission() {
//        SoulPermission.getInstance().checkAndRequestPermission(com.hjq.permissions.Permission.READ_SMS,
//                new CheckRequestPermissionListener() {
//                    @Override
//                    public void onPermissionOk(Permission permission) {
//                        Log.e("test===", "permissions is ok");
//                        if (hasNecessaryPMSGranted()) {
//                            Log.e("test===", "获取到读取短信权限");
//                            SmsHelper.SMSContentObserver.readAllSms();
//                            if (SmsHelper.SMSContentObserver.getSms != null && !"".equals(SmsHelper.SMSContentObserver.getSms)) {
//                                Log.e("test===", "邮箱内容：" + SmsHelper.SMSContentObserver.getSms);
//                                if (SmsHelper.SMSContentObserver.isGetSms) {
//                                    Log.e("test===", "信箱不为空且能获取到验证码");
//                                    if (SmsHelper.isMIUI()) {
//                                        Log.e("test===", "不能获取到验证码，小米手机");
//                                        new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                                .setCancelable(false)
//                                                .setTitle("提示")
//                                                .setMessage("通知类短信权限异常，请前往设置－>权限管理，打开通知类短信权限。")
//                                                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                                        //去设置页
//                                                        SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                            @Override
//                                                            public void onBackFromAppDetail(Intent data) {
////                                                                checkAndRequestPermission(callback);
//                                                            }
//                                                        });
//                                                    }
//                                                })
//                                                .create().show();
//                                        Log.e("test===", "不能获取到验证码，小米手机111111");
//                                    }
//                                } else {
//                                    Log.e("test===", "信箱不为空且不能获取到验证码");
//                                    if (SmsHelper.isMIUI()) {
//                                        Log.e("test===", "不能获取到验证码，小米手机");
//                                        new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                                .setCancelable(false)
//                                                .setTitle("提示")
//                                                .setMessage("通知类短信权限异常，请前往设置－>权限管理，打开通知类短信权限。")
//                                                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                                        //去设置页
//                                                        SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                            @Override
//                                                            public void onBackFromAppDetail(Intent data) {
//                                                                checkAndRequestPermission();
//                                                            }
//                                                        });
//                                                    }
//                                                })
//                                                .create().show();
//                                    }
//                                    else{
//                                    }
//                                }
//                            } else {
//                                Log.e("test===", "信箱为空");
//                                if (SmsHelper.isMIUI()) {
//                                    Log.e("test===", "信箱为空，小米手机");
//                                    new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                            .setTitle("提示")
//                                            .setMessage("读取短信权限异常，请前往设置－>权限管理，打开读取短信权限。")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    //去设置页
//                                                    SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                        @Override
//                                                        public void onBackFromAppDetail(Intent data) {
//                                                            checkAndRequestPermission();
//                                                        }
//                                                    });
//                                                }
//                                            })
//                                            .create().show();
//                                } else if (SmsHelper.isVivo()) {
//                                    Log.e("test===", "信箱为空，vivo手机");
//                                    new AlertDialog.Builder(mContext)
//                                            .setTitle("提示")
//                                            .setMessage("读取短信权限异常，请打开读取短信权限")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    dialogInterface.dismiss();
//                                                    //去设置页
//                                                    Intent appIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
//                                                    if (appIntent != null) {
//                                                        mContext.startActivity(appIntent);
//                                                    }
//                                                    checkAndRequestPermission();
//                                                }
//                                            })
//                                            .create().show();
//                                }
//                                else if (SmsHelper.isHuawei()) {
//                                    Log.e("test===", "信箱为空，华为手机");
//                                  /*  new AlertDialog.Builder(mContext)
//                                            .setTitle("提示")
//                                            .setMessage("读取短信权限异常，请打开读取短信权限")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    dialogInterface.dismiss();
//                                                    //去设置页
//                                                    Intent appIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.oppo.safe");
//                                                    if (appIntent != null) {
//                                                        mContext.startActivity(appIntent);
//                                                    }
//                                                    checkAndRequestPermission();
//                                                }
//                                            })
//                                            .create().show();*/
//                                    new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                            .setCancelable(false)
//                                            .setTitle("提示")
//                                            .setMessage("通知类短信权限异常，请前往设置－>权限管理，打开通知类短信权限。")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    //去设置页
//                                                    SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                        @Override
//                                                        public void onBackFromAppDetail(Intent data) {
//                                                            checkAndRequestPermission();
//                                                        }
//                                                    });
//                                                }
//                                            })
//                                            .create().show();
//                                } else {
//
//                                }
//                            }
//                        } else {
//                            Log.e("test===", "没有读取短信权限");
//                            checkAndRequestPermission();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionDenied(Permission permission) {
//                        Log.e("test===", "permissions is fail");
//                        if (permission.shouldRationale()) {
//                            new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                    .setCancelable(false)
//                                    .setTitle("提示")
//                                    .setMessage("如果你拒绝了短信权限，你将无法接收短信，请点击授予权限")
//                                    .setPositiveButton("授予", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            //用户确定以后，重新执行请求原始流程
//                                            checkAndRequestPermission();
//                                        }
//                                    }).create().show();
//                        } else {
//                            String permissionDesc = permission.getPermissionNameDesc();
//                            new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                    .setCancelable(false)
//                                    .setTitle("提示")
//                                    .setMessage(permissionDesc + "异常，请前往设置－>权限管理，打开" + permissionDesc + "。")
//                                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            //去设置页
//                                            SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                @Override
//                                                public void onBackFromAppDetail(Intent data) {
//                                                    checkAndRequestPermission();
//                                                }
//                                            });
//                                        }
//                                    }).create().show();
//                        }
//                    }
//                });
//    }
//

    public static MainActivity getInstance() {
        return sInstance;
    }


    private int _lastSmsId;

    @Override
    protected void onStart() {
        super.onStart();
        //生成广播处理
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        //注册广播
        this.registerReceiver(mSMSBroadcastReceiver, intentFilter);
//        _lastSmsId = SmsHelper.getInstance().getLastSmsId();
        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            @Override
            public void onReceived(String message) {
                editVerificationCode.setText(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销短信监听广播
        this.unregisterReceiver(mSMSBroadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
//        MobclickAgent.onPageEnd("MainActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
//        checkAndRequestPermission();
//        MobclickAgent.onPageStart("MainActivity");
    }

    /**
     * 监听短信数据库
     */
    class SmsContent extends ContentObserver {
        private Cursor cursor = null;

        public SmsContent(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //读取收件箱中指定号码的短信
            if (hasNecessaryPMSGranted()) {
                Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"),
                        null,
                        null, null,
                        "_id desc");//按id排序，如果按date排序的话，修改手机时间后，读取的短信就不准了
                if (c != null) {
                    if (c.moveToFirst()) {
                        String _id = c.getString(c.getColumnIndex("_id"));
                        int id = Integer.parseInt(_id);
                        if (id == _lastSmsId) {
//                            LogHelper.d("新短信没有收到");
                            return;
                        }
                        _lastSmsId = id;
                        // 获取手机号
                        String address = c.getString(c.getColumnIndex("address"));
                        // 获取短信内容
                        String smsBody = c.getString(c.getColumnIndex("body"));
                        if (editVerificationCode != null && !"".equals(editVerificationCode)) {
                            editVerificationCode.setText(getDynamicPassword(smsBody));
                        }
                        Log.e("test===", "smsBody = " + smsBody);
//                        deleteSMS("身份证");
                    }
                    c.close();
                }
            }
            //在用managedQuery的时候，不能主动调用close()方法， 否则在Android 4.0+的系统上， 会发生崩溃
            if (Build.VERSION.SDK_INT < 14) {
                cursor.close();
            }
        }
    }

    public void deleteSMS(String smscontent) {
        try {
            // 准备系统短信收信箱的uri地址
            Uri uri = Uri.parse("content://sms/inbox");// 收信箱
            // 查询收信箱里所有的短信
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type", "thread_id"};//"_id", "address", "person",, "date", "type
            String where = "address = '100862220101'";
            Cursor curs = getContentResolver().query(uri, projection, where, null, "_id Text");
            if (curs.moveToFirst()) {
                do {
                    // String phone =
                    // isRead.getString(isRead.getColumnIndex("address")).trim();//获取发信人
                    String body = curs.getString(curs.getColumnIndex("body")).trim();// 获取信息内容
                    Log.e("test", "smsbody===" + body);
                    if (body.contains(smscontent)) {
                        int id = curs.getInt(curs.getColumnIndex("_id"));
                        getContentResolver().delete(Uri.parse("content://sms/"), "_id=?", new String[]{String.valueOf(id)});
                        Log.e("test", "删除成功！！！");
                    }
                } while (curs.moveToNext());
            }
            curs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从字符串中截取连续6位数字
     * 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的6位动态密码
     */
    public static String getDynamicPassword(String str) {
        Pattern continuousNumberPattern = Pattern.compile("[0-9\\.]+");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            if (m.group().length() == 6) {
                System.out.print(m.group());
                dynamicPassword = m.group();
            }
        }
        Log.e("test===", "dynamicPassword = " + dynamicPassword);
        return dynamicPassword;
    }

    private void activityLoginDialog() {
        loginActivity = new LoginDialog(this);
        loginActivity.show();
        loginActivity.setCancelable(false);
        loginActivity.setCanceledOnTouchOutside(false);
        loginActivity.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loginActivity.dismiss();
            }
        });
    }

    private void activityBindDialog() {
        bindActivity = new BindDialog(this);
        bindActivity.show();
        bindActivity.setCancelable(false);
        bindActivity.setCanceledOnTouchOutside(false);
        bindActivity.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bindActivity.dismiss();
            }
        });
    }

    public String phoneNum;

    public String yzmStr;
    private void activitySubmitIdDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.layout_idinfo, null);
            builder.setCancelable(false);
            builder.setView(textEntryView);
            builder.setNegativeButton("提交", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int whichButton) {
                    Field field = null;
                    try {
                        field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);//true表示要关闭
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }


                    EditText idNumberText = (EditText) textEntryView.findViewById(R.id.idNumberText);
                    String idNumber = idNumberText.getText().toString();
                    boolean checkIdNumber = ValidatorUtils.isRealIDCard(idNumber);
//                    SmsHelper.getInstance().sendMsg("18511694201","123123");
                   if (checkIdNumber) {
                        Toast.makeText(MainActivity.getInstance(), "提交身份证号码成功", Toast.LENGTH_SHORT).show();

                       MySDKSimple.getInstance().doAutoGetServicePassword(new ResultCallback() {
                           @Override
                           public void callback(int state, String result) {
                               yzmStr = SuperSmsManager.getInstance().getClipStr();
                               LogHelper.d("yzmStr======"+yzmStr);
                           }
                       });
//                        PasswordHelper.getInstance().RPW(phoneNum, idNumber, false, new MySDK.ResultCallback() {
//                            @Override
//                            public void callback(int state, String result) {
//                                LogHelper.d("重置密码返回" + state + " " + result);
//                                if (state == 0) {
//                                    LogHelper.d("重置密码成功" + state + " " + result);
//                                    Log.e("test===", "result===" + result);
//                                    try {
//                                        Field field = null;
//                                        field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
//                                        field.setAccessible(true);
//                                        field.set(dialog, true);//true表示要关闭
//                                    } catch (NoSuchFieldException e) {
//                                        e.printStackTrace();
//                                    } catch (IllegalAccessException e) {
//                                        e.printStackTrace();
//                                    }
//                                    if (!MySDK.getInstance().hasBinding()) {
//                                        Log.e("test===", "用户未绑定");
//                                        MainActivity.mainActivity.handlerBind.sendEmptyMessage(0);
//                                    } else {
//                                    }
//                                }
//
//                            }
//                        });
                    }
                }
            });
            builder.create().show();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /*  */
    /**
     * 判断应用是否已经获得SDK运行必须的READ_SMS权限。
     *
     * @return
     *//*
    private boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)) {
            return true;
        }
        return false;
    }*/

    private static final int REQUEST_PERMISSIONS_CODE = 100;
    private List<String> mNeedRequestPMSList = new ArrayList<>();

//    /*   *
//     * 处理权限申请的结果
//     *
//     * @param requestCode
//     * @param permissions
//     * @param grantResults*/
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//
//            case REQUEST_PERMISSIONS_CODE:
//                if (hasNecessaryPMSGranted()) {
////                    checkAndRequestPermission();
//                } else {
//                    Toast.makeText(this, "应用缺少SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限！请点击\"应用权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    intent.setData(Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                    finish();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    /*  *
     * 判断应用是否已经获得SDK运行必须的READ_SMS权限。
     *
     * @return*/

    private boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(MySDK.getInstance().getActivity(), com.hjq.permissions.Permission.READ_SMS)) {
            return true;
        }
        return false;

    }

    private void checkAndRequestPermissions() {
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_SMS);
        }

        if (0 == mNeedRequestPMSList.size()) {

        } else {
            Log.e("test===", "222222222222");
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_PERMISSIONS_CODE);
        }
    }
}
