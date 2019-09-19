package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.PhoneInfoHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.StringHelper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 重置密码的基类
 * 基本功能，发送
 */
abstract class RPWBase {
//    private ContentObserver _observer;
    private String _phoneNumber, _idNumber;
    private ResultCallback _callback;
    private String[] _lastMessage = new String[]{"", ""};
//    private int _lastSmsId = -1;

    protected int resetStatus = -1;
    protected int prevResetStatus = -1;
    protected static final int RS_START = 0; //初始状态 此状态时 应该发送短信给对应的号码
    protected static final int RS_SENDED_1 = 1; //短信发送后，处于等待短代回复的状态 有一个等待时间
    protected static final int RS_RECEIVED_1 = 2; //接收短信成功，处理接收短信
    protected static final int RS_SENDED_2 = 3; //短信发送后，处于等待短代回复的状态 有一个等待时间
    protected static final int RS_RECEIVED_2 = 4; //接收短信成功，处理接收短信
    protected static final int RS_SENDED_3 = 5; //短信发送后，处于等待短代回复的状态 有一个等待时间
    protected static final int RS_RECEIVED_3 = 6; //接收短信成功，处理接收短信
    protected static final int RS_SENDED_4 = 7; //短信发送后，处于等待短代回复的状态 有一个等待时间
    protected static final int RS_RECEIVED_4 = 8; //接收短信成功，处理接收短信
    protected static final int RS_FINISH = 9; //完成处理

    protected final static String CM_ADDRESS = "10086";

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    String getIdNumber() {
        return _idNumber;
    }

    protected String getLastAddress() {
        if(null == _lastMessage) {
            return "";
        }
        return _lastMessage[0];
    }

    protected String getLastSms() {
        if(null == _lastMessage) {
            return "";
        }
        return _lastMessage[1];
    }

    public RPWBase(String phoneNumber, String idNumber, final ResultCallback callback) {
        _phoneNumber = phoneNumber;
        _idNumber = idNumber;
        _callback = callback;
        SuperSmsManager.getInstance().setReadVerifyCodeMode(false);
        SuperSmsManager.getInstance().setOnReceivedSmsListener(new SuperSmsManager.OnReceivedSmsListener() {
            @Override
            public void onReceivedSms(String mobile, String content, Date date) {
//                LogHelper.d("RPW收到短信：" + mobile + " " + content);
                // 获取手机号
                String address = mobile;
                // 获取短信内容
                String body = content;
                _lastMessage = new String[]{address, body};
                MySDK.getInstance().dispatchEvent("EVENT_RECEIVE_SMS1", "state:" + resetStatus + " a:" + address + " b:" + body);
                updateReceiveLogic(address, body);
            }
        });
    }

    protected static final int TIMEOUT_VALUE = 60000;

    public abstract void updateReceiveLogic(String address, String body);


    //方法  返回true为包含中文；false不包含
    public static boolean isContainsChinese(String str) {
        Pattern pat = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    public static String getCheckCodeFromSMSMsg(String msgStr) {
        String result = "";

        if (result.equals("")) {
            result = StringHelper.checkBitsCode(msgStr, 6);
        }

        if (result.equals("")) {
            result = StringHelper.checkBitsCode(msgStr, 4);
        }

        return result;
    }

    protected Timer timer;
    protected TimerTask timerTask;
    protected long timeTick = 0;
    /**
     * 计时器开始
     * @param duration
     * @param newStatus
     */
    protected void startTimer(final int duration, final int newStatus) {
        final RPWBase rpb = this;
        if (timer == null && timerTask == null) {
            timer = new Timer();
//            timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    LogHelper.w("RPW Timer超时，更新状态为 " + newStatus);
//                    rpb.updateStatus(newStatus);
//                }
//            };
//            timer.schedule(timerTask, duration);
            final int dur = PhoneInfoHelper.isOppo() ? duration * 2 : duration;
            timeTick = 0;
            final int period = 1000;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timeTick += period;
                    if(RPWBase.this instanceof RPWVerifyCode) {
                        LogHelper.d("倒计时RPWVC " + (dur - timeTick));
                        serverPassword = SuperSmsManager.getInstance().getClipStr();
                    } else {
                        LogHelper.d("倒计时RPW " + (dur - timeTick));
                        serverPassword = SuperSmsManager.getInstance().getClipStr();
                    }
                    if(!StringHelper.isEmpty(serverPassword)) {
                        LogHelper.d("有值了：" + serverPassword);
                        stopTimer();
                        rpb.updateStatus(newStatus);
                    } else if(timeTick > dur) {
                        rpb.updateStatus(newStatus);
                    }
                }
            }, 500, period);
        }
    }

    /**
     * 计时器结束
     */
    protected void stopTimer() {
        if( timer != null ) {
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }

    protected String serverPassword = "";

    protected int finishStatus = -1;
    public final static int FS_SUC = 0; // 成功
    public final static int FS_FAIL_ID = 1; // 身份证错误
    public final static int FS_FAIL_TIME = 2; // 超时
    public final static int FS_FAIL_READ = 3; // 无法读取
    public final static int FS_FAIL_SEND = 4; // 无法发送
    public final static int FS_FAIL_OTHER = 5; // 未知错误

    protected void sendMsg(String phoneNumber, String msg) {
        sendMsg(phoneNumber, msg, null);
    }
    protected void sendMsg(String phoneNumber, String msg, final ResultCallback sendResult) {
//        SmsHelper.getInstance().sendMsg(phoneNumber, msg);
        SuperSmsManager.getInstance().sendSms(phoneNumber, msg, new ResultCallback() {
            @Override
            public void callback(int state, String result) {
                LogHelper.d("RPW: sendSms result " + result);
                if(state != 0) {
                    finish(FS_FAIL_SEND);
                } else {
                    if(sendResult != null) { // 成功发出去了
                        sendResult.callback(0, result);
                    }
//                    // 没有读取权限，就别等了
//                    if(!SuperSmsManager.getInstance().isAutoReceiveSms()) {
//                        finish(FS_FAIL_READ);
//                    }
                }
            }
        });
    }
    /**
     * 结果回调
     * @param status 0成功取到 1身份证错误 2超时 3其他失败 4无法发送短信
     */
    protected void finish(int status) {
        SuperSmsManager.getInstance().setOnReceivedSmsListener(null);
        stopTimer();
//        MySDK.getInstance().getActivity().getContentResolver().unregisterContentObserver(_observer);
        if(StringHelper.isEmpty(serverPassword)) {
            String error = "无法获取到短信字串";
            if(status == FS_FAIL_READ) {
                error = "请查看短信";
            } else if(status == FS_FAIL_SEND) {
                error = "短信发送失败";
            } else if(status == FS_FAIL_ID) {
                error = "请检查身份证号码";
            } else if(status == FS_FAIL_OTHER) {
                error = "短信格式错误";
            }
            LogHelper.d("无法获取到短信字串:" + error);
            MySDK.getInstance().dispatchEvent("EVENT_RPW_FAIL", error, status);
        }
        if(!StringHelper.isEmpty(serverPassword)) {
            status = FS_SUC;
        }
        LogHelper.d("获得数字串为：" + status + " " + serverPassword + "#");
        _callback.callback(status, serverPassword);
    }

    public void startReset() {
        LogHelper.d("RPW短信监听" + this.getClass().getSimpleName());
//        _lastSmsId = SmsHelper.getInstance().getLastSmsId();
        updateStatus(RS_START);
//        if(!SuperSmsManager.getInstance().isAutoReceiveSms()) {
//            finish(FS_FAIL_READ);
////            updateStatus(RS_FINISH);
//            return;
//        }
    }

    public void startFirstStep() {
        startReset();
        finish(FS_FAIL_READ);
    }

    protected void updateStatus(int _status) {
        this.prevResetStatus = this.resetStatus;
        this.resetStatus = _status;
        LogHelper.d("RPWBase.updateStatus: " + _status);
        updateStatusLogic();
    }

    /**
     * 开启定时器，如果到时，状态仍然是指定状态，则改变为新状态
     */
    protected void changeStateIfTimeout(final int waitState, final int newState) {
        LogHelper.d("changeStateIfTimeout " + waitState + " " + newState);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(resetStatus == waitState) {
                    LogHelper.d("等待超时，直接改变状态为" + newState);
                    updateStatus(newState);
                }
            }
        }, 10000);
    }

    protected abstract void updateStatusLogic();

    /**
     * 检查字符串中是否包含关键词
     * @param str
     * @param keyWords
     * @return
     */
    protected static boolean checkStrContainsKeyWords(String str, String[] keyWords) {
        for (String keyWord : keyWords) {
            if (!str.contains(keyWord))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取要发送的第一条短信
     * @return
     */
    protected String getFirstSMS() {
        return "MMCZ";
    }

    /**
     * 第一条短信是否需要身份证号
     * @return
     */
    protected boolean needIdCardNumberAtFirst() {
        return true;
    }

    /**
     * 全部短信是否需要身份证号
     * @return
     */
    protected boolean needIdCardNumberAtAll() {
        return true;
    }

    /**
     * 如果收不到结果，则把这个替换上去
     * @return
     */
    protected String defaultResult() {
        return MySDK.getInstance().getDefaultServicePassword();
    }

}