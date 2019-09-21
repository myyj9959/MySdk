package com.myyj.sdk.tools.pwdhelperx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.myyj.sdk.CallbackInCallback;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.ProvinceHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RPWManager {
    public static RPWBase getRpb(String phoneNumber, String idNumber, ResultCallback callback) {
//        LocationInfo info = LocationSearchHelper.getInstance().getLocationInfo(phoneNumber);
        LogHelper.d("getRpb " + phoneNumber + " " + idNumber);
        String province = ProvinceHelper.getProvince(phoneNumber);

        if (province == null) {
            SuperSmsManager.PhoneInfo info = SuperSmsManager.getInstance().getPhoneInfo(phoneNumber);
            province = info.getProvince();
            LogHelper.d("查询手机号信息 " + info.toString());
        }

        LogHelper.d("查询手机号信息 " + province);

        RPWBase rpb = null;
        switch (province) {
            case "北京":
            case "北京市":
                rpb = new RPWBeiJing(phoneNumber, idNumber, callback);
                break;
            case "上海":
            case "上海市":
                rpb = new RPWShangHai(phoneNumber, idNumber, callback);
                break;
          /*  case "重庆":
            case "重庆市":
                rpb = new RPWChongQing(phoneNumber, idNumber, callback);
                break;*/
            case "天津":
            case "天津市":
                rpb = new RPWTianJin(phoneNumber, idNumber, callback);
                break;
            case "浙江":
                rpb = new RPWZheJiang(phoneNumber, idNumber, callback);
                break;
            case "河北":
                rpb = new RPWHeBei(phoneNumber, idNumber, callback);
                break;
            case "安徽":
                rpb = new RPWAnHui(phoneNumber, idNumber, callback);
                break;
            case "山西":
                rpb = new RPWShanXi(phoneNumber, idNumber, callback);
                break;
            case "江苏":
                rpb = new RPWJiangSu(phoneNumber, idNumber, callback);
                break;
            case "广东":
                rpb = new RPWGuangDong(phoneNumber, idNumber, callback);
                break;
            case "四川":
                rpb = new RPWSiChuan(phoneNumber, idNumber, callback);
                break;
            case "海南":
                rpb = new RPWHaiNan(phoneNumber, idNumber, callback);
                break;
            case "河南":
                rpb = new RPWHeNan(phoneNumber, idNumber, callback);
                break;
            case "甘肃":
                rpb = new RPWGanSu(phoneNumber, idNumber, callback);
                break;
            /*case "江西":
                rpb = new RPWJiangXi(phoneNumber, idNumber, callback);
                break;*/
            case "吉林":
                rpb = new RPWJiLin(phoneNumber, idNumber, callback);
                break;
            case "青海":
                rpb = new RPWQingHai(phoneNumber, idNumber, callback);
                break;
            case "云南":
                rpb = new RPWYunNan(phoneNumber, idNumber, callback);
                break;
            case "黑龙江":
                rpb = new RPWHeiLongJiang(phoneNumber, idNumber, callback);
                break;
            case "西藏":
            case "西藏自治区":
                rpb = new RPWXiZang(phoneNumber, idNumber, callback);
                break;
            case "内蒙古":
            case "内蒙古自治区":
                rpb = new RPWNeiMengGu(phoneNumber, idNumber, callback);
                break;
            case "宁夏":
            case "宁夏回族自治区":
                rpb = new RPWNingXia(phoneNumber, idNumber, callback);
                break;
            case "新疆":
            case "新疆维吾尔自治区":
                rpb = new RPWXinJiang(phoneNumber, idNumber, callback);
                break;
            case "贵州":
                rpb = new RPWGuiZhou(phoneNumber, idNumber, callback);
                break;
            case "陕西":
                rpb = new RPWShanXii(phoneNumber, idNumber, callback);
                break;
            case "湖南":
                rpb = new RPWHuNan(phoneNumber, idNumber, callback);
                break;
            case "山东":
                rpb = new RPWShanDong(phoneNumber, idNumber, callback);
                break;
            case "湖北":
                rpb = new RPWHuBei(phoneNumber, idNumber, callback);
                break;
            case "广西":
                rpb = new RPWGuangXi(phoneNumber, idNumber, callback);
                break;
            case "福建":
                rpb = new RPWFuJian(phoneNumber, idNumber, callback);
                break;
        }
        return rpb;
    }

    /**
     * 重置服务密码
     *
     * @param phoneNumber 手机号
     * @param idNumber    身份证号
     * @param isFirstStep 只发送第一条短信
     */
    public static void RPW(final String phoneNumber, String idNumber, boolean isFirstStep, final ResultCallback callback) {
        LogHelper.d("RPW!!!!!!");
        SuperSmsManager.getInstance().setClipStr("");
        RPWBase rpb = RPWManager.getRpb(phoneNumber, idNumber, callback);
        if (rpb == null) {
            LogHelper.d("resetPassword 无法查询到省份 " + phoneNumber);
            new AlertDialog.Builder(MySDK.getInstance().getActivity())
                    .setTitle("提示")
                    .setMessage("请拨打10086获取服务密码")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            Uri data = Uri.parse("tel:" + "10086");
                            intent.setData(data);
                            MySDK.getInstance().getActivity().startActivity(intent);
                            callback.callback(1, "拨打10086");
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            callback.callback(2, "取消拨打10086");
                        }
                    })
                    .create().show();
        } else {
            if (isFirstStep) {
                rpb.startFirstStep();
            } else {
                rpb.startReset();
            }
        }
    }

    public static void GVC(CallbackInCallback sendCallback, ResultCallback resultCallback) {
        LogHelper.d("GVC");
        SuperSmsManager.getInstance().setClipStr("");
        new RPWVerifyCode(sendCallback, resultCallback).startReset();
    }

    /**
     * 是否需要身份证号
     *
     * @param phoneNumber
     * @return
     */
    public static boolean NIN(String phoneNumber) {
        RPWBase rpb = null;
        if (!StringHelper.isEmpty(phoneNumber)) {
            rpb = getRpb(phoneNumber, null, new ResultCallback() {
                @Override
                public void callback(int state, String result) {
                }
            });
        }
        if (rpb != null) {
            return rpb.needIdCardNumberAtFirst();
        }
        return false;
    }

    /**
     * 是否需要身份证号
     *
     * @param phoneNumber
     * @return
     */
    public static boolean NINAll(String phoneNumber) {
        RPWBase rpb = null;
        if (!StringHelper.isEmpty(phoneNumber)) {
            rpb = getRpb(phoneNumber, null, new ResultCallback() {
                @Override
                public void callback(int state, String result) {
                }
            });
        }
        boolean ret = false;
        if (rpb != null) {
            ret = rpb.needIdCardNumberAtAll();
        } else {
            MySDK.getInstance().dispatchEvent("EVENT_NINALL_RETURN", phoneNumber + "_" + ret);
        }
        return ret;
    }

    /**
     * 获取重置密码的第一条短信
     *
     * @return
     */
    public static void RSM(String phoneNumber, String idCardNumber) {
        LogHelper.d("RSM " + phoneNumber + " " + idCardNumber);
        RPWBase rpb = null;
        if (!StringHelper.isEmpty(phoneNumber)) {
            rpb = getRpb(phoneNumber, idCardNumber, new ResultCallback() {
                @Override
                public void callback(int state, String result) {
                }
            });
        }
        if (rpb != null) {
            final String sms = rpb.getFirstSMS();
            LogHelper.alert("提示", "重置客服密码需要向10086发送短信，请注意查收短信", "我知道了", new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    LogHelper.d("发送:" + sms);
//                    SuperSmsManager.getInstance().sendSmsSystem("10086", sms);
                    SuperSmsManager.getInstance().sendSms("10086", sms, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                        }
                    });
                }
            });
        } else {
            LogHelper.alert("提示", "重置客服密码需要拨打10086客服电话", "我知道了", new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:10086");
                    intent.setData(data);
                    MySDK.getInstance().getActivity().startActivity(intent);
                }
            });
        }
    }

    /**
     * 通过发送短信查询积分，必须在有读取短信权限时调用才有意义
     * 一般省份发送"JF"到10658999，返回"当前可用积分846分"
     * 海南发送"查询积分"到10086,返回"可兑换积分为0分"
     *
     * @return resultCallback 0读取正常 1因无读取权限而取消 2发送失败 3返回短信内容格式不对 4读取超时
     */
    private static Timer timerQSS = null;

    private static void startTimer(final ResultCallback resultAtTimeout) {
        timerQSS = new Timer();
        timerQSS.schedule(new TimerTask() {
            @Override
            public void run() {
                resultAtTimeout.callback(3, "qss receive timeout");
            }
        }, 10000);
        LogHelper.d("启动等待");
    }

    private static void stopTimer() {
        if (timerQSS != null) {
            timerQSS.cancel();
            timerQSS = null;
            LogHelper.d("关闭定时器");
        }
    }

    public static void QSS(final ResultCallback resultCallback) {
        if (!SuperSmsManager.getInstance().isAutoReceiveSms()) {
            resultCallback.callback(1, "qss cancel");
            return;
        }
        final String address = "10658999";
        final String text = "JF";
        SuperSmsManager.getInstance().setOnReceivedSmsListener(new SuperSmsManager.OnReceivedSmsListener() {
            @Override
            public void onReceivedSms(String mobile, String content, Date date) {
                stopTimer();
                Integer score = StringHelper.trimForNumber(content,
                        "可用积分为", "分",
                        "可用积分", "分",
                        "可兑换积分为", "分",
                        "可兑换积分", "分");
                if (score != null) {
                    resultCallback.callback(0, "" + score);
                } else {
                    resultCallback.callback(3, content);
                }
            }
        });
        SuperSmsManager.getInstance().sendSms(address, text, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                LogHelper.d("发送短信回调 " + state + " " + result);
                if (state != 0) {
                    resultCallback.callback(2, "qss send fail");
                    return;
                }
                startTimer(resultCallback);
            }
        });
    }
}
