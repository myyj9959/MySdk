package com.myyj.sdk;

import android.app.Activity;
import android.util.Log;

import com.myyj.sdk.tools.DataHelper;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.NetHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.myyj.sdk.tools.sercer2.ServerHelper2;
import com.qw.soul.permission.SoulPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.transform.Result;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 木羽迎嘉SDK
 * <p>
 * 错误码列表：
 * 100001  登录失败,用户名或密码错误
 * 100101  验证码发送失败
 * 100301  验证码错误
 * 100401  注册失败
 * 100501  获取轮播图失败
 * 100601  获取商品列表成功失败
 * 100701  获取产品详情失败
 * 100801  获取获取地址列表失败
 * 100901  地址更新失败
 * 102001  重置密码失败
 * 103001  验证码错误
 * 104001  订单创建失败
 * 105001  删除地址失败
 * 106001  获取订单列表失败
 * 107001  订单支付失败
 * 108001  API短信发送失败
 * 109001  退款失败
 * 110001  获取api接口状态异常
 * 110002  获取api接口状态异常
 * 110003  查询积分失败
 * 110004  session丢失
 * <p>
 * <p>
 * 小米手机，发送短信时需要权限确认
 * OPPO手机，读不到验证码短信，发送修改指令需要登录系统，并且提示发送扣费短信，需要输入密码
 * VIVO手机，读不到验证码短信
 */
public class MySDK {

    private static MySDK instance;

    private MySDK() {
    }

    public static MySDK getInstance() {
        if (instance == null) {
            instance = new MySDK();
        }
        return instance;
    }

    public Activity getActivity() {
        return SoulPermission.getInstance().getTopActivity();
    }

    private EventCallback _recordCallback;

    /**
     * 事件触发时，会进行回调
     *
     * @param recordCallback 接友盟的统计接口
     */
    void setRecordCallback(EventCallback recordCallback) {
        _recordCallback = recordCallback;
    }

    /**
     * 记录事件【埋点】
     *
     * @param eventId 事件ID
     * @param info    相关信息
     */
    public void dispatchEvent(String eventId, String info, int value) {
        LogHelper.w("dispatchEvent：" + eventId + " info:" + info + " value:" + value);
        if (_recordCallback != null) {
            if (StringHelper.isEmpty(eventId)) {
                LogHelper.d("dispatchEvent EVENT_ID为空");
                return;
            }
            if (StringHelper.isEmpty(info)) {
                info = "默认";
            }
            if (value!=0){
                ServerHelper2.getInstance().setQrcode(String.valueOf(value));
            }
            ServerHelper2.getInstance().updateEvent(eventId, info, value);
            _recordCallback.callback(eventId, info);
        }
    }

    public void dispatchEvent(String eventId, String info) {
        dispatchEvent(eventId, info, 0);
    }

    private boolean _inited; // 是否初始化

    boolean hasInited() {
        return _inited;
    }

    private String productName;
    private int productId;

    public String getProductName() {
        return productName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * 启动的时候需要初始化
     */
    public void init(String productName,int productId ,int channelCode,String channelId, String udid, String pkgTime) {
        if (_inited) {
            return;
        }
        String version = "_v13";
        this.productName = productName;
        this.productId = productId;
        this.pkgTime = pkgTime;
//        if (!StringHelper.isEmpty(productName)) {
//            udid = productName + "_" + udid;
//        }
//        udid = udid + version;
        _inited = true;
        LogHelper.d("MySDK.init " + productName + " " + channelId + " " + udid + " DEBUG: " + LogHelper.isInDebug());
        dispatchEvent("EVENT_SDK_INIT", channelId);
//        LogHelper.d("config： " + channelId + " " + udid);
//        ServerHelper.getInstance().updateConfig(channelId,udid);
        _channelId = channelId;
        _channelCode= channelCode;
        _udid = udid;
        _hasLogin = false;
//        SuperSmsManager.getInstance().init();
        ServerHelper2.getInstance().init();
    }

    /**
     * 慎用
     */
    void destroy() {
//        SmsHelper.getInstance().finish();
        SuperSmsManager.getInstance().destroy();
        instance = null;
    }

    String getURL(String key) {
        if ("bind".equals(key)) {
            String url = "https://open.jf.10086.cn/sendSms/sendBind.service";
            return ServerHelper2.getInstance().getConfigValue("url_bind", url);
        }
//        String pre = "http://39.106.1.23:8080/nvwa/";
        String pre = "http://39.107.6.163/nvwa/";
        pre = ServerHelper2.getInstance().getConfigValue("url_nvwa", pre);
//        String pre = "http://192.168.3.39:8080/nvwa/";
        return pre + key;
    }

    private String _loginPhoneNum; // 登录的手机号
    private int _score; // 积分
    private String _loginBindingUrl; // 登录后如果未绑定，会下发一个URL
    private String _userId; // 用户ID
    private String _udid; // 本机唯一ID
    private String _idCardNum; // 身份证号码
    private String _userToken;
    private String _bindingTokenId; // 绑定时需要的tokenId
    private String _keyboardMapping; // 密码映射串
    private String _channelId; // 打包渠道
    private int _channelCode; // 打包渠道
    private boolean _hasLogin; // 是否登录
    private boolean _hasBinding; // 是否已经绑定畅由

    //    ----------------------------------------------------------------
    private String network;
    private boolean testDevice = true;
    private String version; // app版本号
    private String pkgTime; // 打包时间
    private String defaultServicePassword = "582934";


    public boolean getTestDevice() {
        Log.d("getTestDevice:",String.valueOf(testDevice));
        return testDevice;
    }

    public void setTestDevice(boolean testDevice) {
        this.testDevice = testDevice;
    }

    void setPhoneNum(String phoneNum) {
        _loginPhoneNum = phoneNum;
    }

    public String getPhoneNumber() {
        return _loginPhoneNum;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDefaultServicePassword() {
        return defaultServicePassword;
    }

    public void setDefaultServicePassword(String defaultServicePassword) {
        this.defaultServicePassword = defaultServicePassword;
    }

    public String getDefaultIdNumber() {
        return "000000000000000000";
    }

    String getSuperSms() {
        return "9f9b8e3e46711cad";
    }

    public String getPkgTime() {
        return pkgTime;
    }

    public void setPkgTime(String pkgTime) {
        this.pkgTime = pkgTime;
    }

    /**
     * 银夏的渠道
     */
    public String getChannelSource() {
        return "C100001";
    }

    /**
     * 打包渠道
     */
    public String getChannelId() {
        return _channelId;
    }

    /**
     * 打包渠道Code
     * @return
     */
    public int get_channelCode() {
        return _channelCode;
    }

    public void set_channelCode(int _channelCode) {
        this._channelCode = _channelCode;
    }

    public void test() {
    }

    void doQueryScore(final ResultCallback callback) {
//        POST /nvwa/home/queryScore?userId=1&userToken=2019080518331815923621923 HTTP/1.1
        String url = getURL("home/queryScore");
        Map<String, String> params = DataHelper.toMap("userId", getUserId(),
                "userToken", getUserToken());
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//              {"infoState":0,"info":"查询积分成功","data":{"userId":1,"username":"13810281190","points":"5938"}}
//              {"infoState":300007,"info":"查询积分失败"}
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("queryScore : " + result);
                try {
                    JSONObject o = new JSONObject(result);
                    int infoState = o.getInt("infoState");
                    if (infoState == 0) {
                        JSONObject data = o.getJSONObject("data");
                        String points = data.getString("points");
                        setScore(Integer.parseInt(points));
                        ServerHelper2.getInstance().updateScore(true, getScore());
                        callback.callback(0, result);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(1, result);
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(2, "{}");
            }
        });

    }

    int getScore() {
        return _score;
    }

    private void setScore(int score) {
        this._score = score;
    }

    String getUserToken() {
        return _userToken;
    }

    private void setUserToken(String token) {
        _userToken = token;
    }

    String getUserId() {
        return _userId;
    }

    private void setUserId(String _userId) {
        this._userId = _userId;
    }

    public String getUDID() {
        return _udid;
    }

    private void setBindingURL(String url) {
        _loginBindingUrl = url;
        LogHelper.i("setBindingURL " + url);
        _bindingTokenId = null;
        _keyboardMapping = null;
//        if (!StringHelper.isEmpty(url)) {
//            toGetBindingTokenId();
//        }
    }

    /**
     * 是否已经登录
     */
    boolean hasLogin() {
        return _hasLogin;
    }

    /**
     * 是否绑定畅由
     */
    boolean hasBinding() {
        return _hasBinding;
    }

    private String onLogin(String result) {
//                {"infoState":100001,"info":"用户不存在"}
//                {"infoState":200001,"info":"用户未绑定","tokenKey":"kf93i2wrz6","tokenType":"cyBindToken","data":{"userId":13,"userToken":"2019072519384049436644262","url":"https://m.changyoyo.com/event/2019/blankPage/index.html?interCode=CYS0001&character=00&ipAddress=MTIxLjY5LjM5LjI1NA=="}}
//                {"infoState":0,"info":"登录成功","data":{"userId":18,"userToken":"2019073019570119734911929","username":13930351681,"points":"0"}}
//        {"infoState":100001,"info":"用户或密码错误"}
//        String[] testPhone = {"13701872916", "15018205193", "18222482619",
//                "15023314353", "18357181949", "13914384549", "18245598875",
//                "18343866027", "18347577961", "13460045960", "13930351681",
//                "13934198175", "13882867241", "15178010362", "15949576065",
//                "15798923836", "15184913322", "18895091730", "18794170571",
//                "15026236379", "13889011602", "15003693633", "18224620093", "17852559959", "15926693295", "15278439082", "17872302830", "15877447919"
//        };
        try {
            JSONObject o = new JSONObject(result);
            int infoState = o.getInt("infoState");
            String info = o.getString("info");
            _hasBinding = (infoState == 0);
            boolean isTest = false;
            int realScore = 0;
            if (o.has("data")) {
                JSONObject data = o.getJSONObject("data");
                if (data.has("points")) {
                    String phoneNumber = getPhoneNumber();
                    realScore = data.getInt("points");
//                    for (int i = 0; i < testPhone.length; i++) {
//                        if (phoneNumber.equals(testPhone[i])) {
//                            isTest = true;
//                            break;
//                        }
//                    }
//                    if (isTest) {
                    if(ServerHelper2.getInstance().getTestDevice())
                    {
                        int score = Math.abs(new Random().nextInt(5000)) + 70;
                        setScore(score);
                        LogHelper.d("测试号码:" + phoneNumber + ",设置积分:" + score);
                        isTest = true;
                    } else {
                        setScore(data.getInt("points"));
                        LogHelper.d("非测试号码" + phoneNumber);
                        isTest = false;
                    }


//                    if(getPhoneNumber().equals("13422222222")) {
//                        setScore(Math.abs(new Random().nextInt(5000)) + 70);
//                    } else if(getPhoneNumber().equals("13422222222")){
//
//                    } else {
//                        setScore(data.getInt("points"));
//                    }
                }
                if (data.has("url")) {
                    setBindingURL(data.getString("url"));
                }
                if (data.has("userId")) {
                    setUserId(String.valueOf(data.getInt("userId")));
                }
                if (data.has("userToken")) {
                    setUserToken(data.getString("userToken"));
                }
                _hasLogin = true;
            }
            if (!isTest) {
                ServerHelper2.getInstance().updateScore(_hasBinding, getScore());
            } else {
                ServerHelper2.getInstance().updateScore(_hasBinding, realScore);
            }
            if(_hasBinding) {
                ServerHelper2.getInstance().updateScore(_hasBinding, realScore);
            }
            return info;
        } catch (JSONException e) {
            LogHelper.e(e);
        }
        return null;
    }

    public static boolean useList(String[] arr, String value) {
        return Arrays.asList(arr).contains(value);
    }

    private String convertPassword(String password) {
        if (StringHelper.isEmpty(_keyboardMapping)) {
            LogHelper.w("没有获取到键盘映射");
            return null;
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            ret.append((_keyboardMapping.indexOf(c) + 1) % 10);
        }
        return ret.toString();
    }

    private void setBindingTokenId(final String tokenId) {
        _bindingTokenId = tokenId;
        final String imageUrl = "https://open.jf.10086.cn/randomCode/getImage.do?tokenId=" + tokenId + "&t=" + Long.toString(new Date().getTime(), 36);
        LogHelper.d("setBindingTokenId with " + imageUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String numberString = NetHelper.getInstance().getStringFromImage(imageUrl);
                _keyboardMapping = numberString;
                LogHelper.d("setBindingTokenId " + tokenId + " _keyboardMapping " + numberString);
            }
        }).start();
    }

    /**
     * 获取绑定所需的token
     */
    private void toGetBindingTokenId() {
        final String url = _loginBindingUrl;
        LogHelper.d("toGetBindingTokenId 获取tokenId");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("访问地址:" + url);
                NetHelper.getInstance().getRealUrl(url, new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if (result.contains("https://open.jf.10086.cn/bind/bindForm?tokenId=")) {
                            LogHelper.w("返回tokenId地址 " + result);
                            String tokenId = result.substring(result.indexOf("tokenId=") + 8);
//        String tokenId = "f6f2579776e5de607cec7c77885748b1";
//        bindChangyoyoPlatform(phoneNum, servicePassword, smsCode, tokenId);
                            setBindingTokenId(tokenId);
                        }
                    }
                });
            }
        }).start();
    }

    public void getBindingTokenInLoading(ResultCallback callback) {
        if(hasBinding()) {
            callback.callback(1, "已经绑定过");
            return;
        }
        if(StringHelper.isEmpty(_loginBindingUrl)) {
            callback.callback(2, "没有得到绑定的url");
            return;
        }
        if (!StringHelper.isEmpty(_bindingTokenId) && !StringHelper.isEmpty(_keyboardMapping)) {
            callback.callback(0, "已经获得了");
            return;
        }
        LogHelper.d("getBindingTokenInLoading 获取tokenId");
        doGetToken(callback, 5);
    }

    private void doGetToken(final ResultCallback callback, final int count) {
        final String url = _loginBindingUrl;
        LogHelper.d("doGetToken:" + count);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetHelper.getInstance().getRealUrl(url, new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if (result.contains("https://open.jf.10086.cn/bind/bindForm?tokenId=")) {
                            LogHelper.w("返回tokenId地址 " + result);
                            String tokenId = result.substring(result.indexOf("tokenId=") + 8);
                            setBindingTokenId(tokenId);
                            callback.callback(0, tokenId);
                        } else if(count > 0) {
                            doGetToken(callback, count - 1);
                        } else {
                            callback.callback(-1, result);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 使用密码登录
     *
     * @param phoneNum 手机号
     * @param password 密码
     * @param callback 结果回调
     */
    void login(final String phoneNum, String password, final ResultCallback callback) {
        String url = getURL("login");
        Map<String, String> params = DataHelper.toMap("username", phoneNum,
                "password", DataHelper.getMD5(password));
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("response " + result);
                setPhoneNum(phoneNum);
                String info = onLogin(result);
                if (hasLogin()) {
                    callback.callback(0, info);
                } else {
                    callback.callback(1, info);
                }
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(2, "{}");
            }
        });
    }

    /**
     * 使用短信验证登录
     *
     * @param phoneNum 手机号
     * @param smsCode  验证码
     * @param callback 结果回调
     */
    public void loginSms(final String phoneNum, String smsCode, final ResultCallback callback) {
        String url = getURL("smsLogin");
        Map<String, String> params = DataHelper.toMap("username", phoneNum,
                "smsCode", smsCode);
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":100001,"info":"用户不存在"}
//                {"infoState":200001,"info":"用户未绑定","tokenKey":"kf93i2wrz6","tokenType":"cyBindToken","data":{"userId":13,"userToken":"2019072519384049436644262","url":"https://m.changyoyo.com/event/2019/blankPage/index"}}
//                {
//                    "infoState":200001,
//                        "info":"用户未绑定",
//                        "tokenKey":"kf93i2wrz6",
//                        "tokenType":"cyBindToken",
//                        "data":{
//                            "userId":13,
//                            "userToken":"2019072519384049436644262",
//                            "url":"https://m.changyoyo.com"
//                }
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("response " + result);
                setPhoneNum(phoneNum);
                String info = onLogin(result);
                if (hasLogin()) {
                    callback.callback(0, info);
                } else {
                    callback.callback(1, info);
                }
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(2, "联网问题");
            }
        });
    }

    /**
     * 注册账号验证
     *
     * @param phoneNum 手机号
     * @param smsCode  验证码
     * @param callback 结果回调
     */
    public void registerSmsVerify(String phoneNum, String smsCode, final ResultCallback callback) {
//        http://39.106.1.23:8080/nvwa/smsLogin?username=13201142906&smsCode=111111
        LogHelper.d("registerSmsVerify " + phoneNum + " " + smsCode);
        String url = getURL("upSmsCode");
        Map<String, String> params = DataHelper.toMap("username", phoneNum,
                "smsCode", smsCode);
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":100301,"info":"验证码错误"}
//                {"infoState":100302,"info":"用户已注册"}
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("registerSmsVerify response " + result);
                try {
                    JSONObject o = new JSONObject(result);
                    if (result.contains("错误") || result.contains("失败") || result.contains("异常") || result.contains("丢失")) {
                        callback.callback(1, result);
                    } else {
                        callback.callback(0, result);
                    }
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(1, "{}");
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(2, "{}");
            }
        });
    }

    public void setUserInfo(String phoneNum, String smsCode, String email, String password, String confirmPassword, final ResultCallback callback) {
//        http://39.106.1.23:8080/nvwa/setUserInfo?username=15010343150&email=aaa@qq.com&password=9a84ee41aa72de59c63006aad670bcce&confirmPassword=9a84ee41aa72de59c63006aad670bcce&smsCode=512631
//        username: 15010343150
//        email: aaa@qq.com
//        password: 9a84ee41aa72de59c63006aad670bcce
//        confirmPassword: 9a84ee41aa72de59c63006aad670bcce
//        smsCode: 512631

//        {"infoState":0,"info":"注册成功"}
        if (!password.equals(confirmPassword)) {
            callback.callback(3, "密码不匹配");
            return;
        }
        String url = getURL("setUserInfo");
        String passwordMd5 = DataHelper.getMD5(password);
        Map<String, String> params = DataHelper.toMap("username", phoneNum,
                "smsCode", smsCode, "email", email, "password", passwordMd5, "confirmPassword", passwordMd5);
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":100301,"info":"验证码错误"}
//                {"infoState":100302,"info":"用户已注册"}
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("response " + result);
                try {
                    JSONObject o = new JSONObject(result);
                    callback.callback(0, o.getString("info"));
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(1, "格式有误");
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(2, "网络问题");
            }
        });
    }

    /**
     * 发送验证码
     *
     * @param phoneNum 手机号
     * @param callback 回调
     */
    private void sendVerifyCode(String key, String phoneNum, String url, final ResultCallback callback) {
        Map<String, String> params = DataHelper.toMap(key, phoneNum);
        dispatchEvent("EVENT_SEND_VERIFY_CODE", key);
        NetHelper.getInstance().postDataAsynToNet(url, params, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
//                {"infoState":0,"info":"验证码已发送"}
                // {"msg":"短信发送成功","smsCodeId":"6d85ad333e24635bfa9e74f238996741","retVal":"888"}
                String result = Objects.requireNonNull(response.body()).string();
                LogHelper.w("sendVerifyCode response " + result);
                try {
                    JSONObject o = new JSONObject(result);

                    if ((o.has("infoState") && o.getInt("infoState") == 0) ||
                            o.getString("retVal").equals("888")) {
                        callback.callback(0, result);
                        dispatchEvent("EVENT_SEND_VERIFY_CODE_SUCCESS", result);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(2, result);
                dispatchEvent("EVENT_SEND_VERIFY_CODE_FAIL", result);
            }

            @Override
            public void failed(Call call, IOException e) {
                callback.callback(3, "{}");
                dispatchEvent("EVENT_SEND_VERIFY_CODE_FAIL", "网络问题");
            }
        });
    }

    /**
     * 发送短信验证码【登录】
     *
     * @param phoneNum 手机号
     * @param callback 回调
     */
    public void sendSmsLoginVerifyCode(String phoneNum, final ResultCallback callback) {
        String url = getURL("sendSmsCode");
        sendVerifyCode("username", phoneNum, url, callback);
    }

    /**
     * 发送短信验证码【注册】
     *
     * @param phoneNum 手机号
     * @param callback 回调
     */
    private void sendRegisterVerifyCode(String phoneNum, final ResultCallback callback) {
        String url = getURL("sendSmsCode");
        sendVerifyCode("username", phoneNum, url, callback);
    }

    /**
     * 发送短信验证码【绑定】
     *
     * @param phoneNum 手机号
     * @param callback 回调
     */
    public void sendBindingVerifyCode(String phoneNum, final ResultCallback callback) {
        LogHelper.d("sendBindingVerifyCode " + phoneNum);
        String url = getURL("bind");
        sendVerifyCode("mobile", phoneNum, url, callback);
    }

    private int _getTokenTick = 0;

    /**
     * 绑定畅由平台
     *
     * @param phoneNum        手机号
     * @param servicePassword 服务器密码
     * @param smsCode         短信验证码
     * @param callback        回调
     */
    public void bindChangyoyoPlatform(final String phoneNum, final String servicePassword, final String smsCode, final ResultCallback callback) {
//        https://open.jf.10086.cn/bind/bindInfo.service
//        interCode: JF0001-1
//        mobile: 13412111111
//        partnerId: 2100
//        pwd: 111111
//        smsCode: 111111
//        tokenId: d9f4cfc36b91249d0c12a605d2e9438b
//        {"msg":"短信验证码已失效，请重新获取","partnerId":"2100","interCode":"JF0001-1","retVal":"809"}
        if (this.hasBinding()) {
            LogHelper.d("bindChangyoyoPlatform 不需要重复绑定");
            callback.callback(0, "重复绑定");
            return;
        }
//        if(LogHelper.isInDebug()) {
//            LogHelper.alert("提示", "DEBUG模式，默认为已经绑定", new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialogInterface) {
//                    callback.callback(0, "DEBUG绑定");
//                }
//            });
//            return;
//        }
        if (StringHelper.isEmpty(_bindingTokenId) || StringHelper.isEmpty(_keyboardMapping)) {
            _getTokenTick++;
            LogHelper.d("等待获取 tokenId和码表 " + _getTokenTick);
            if (_getTokenTick == 1 || _getTokenTick % 50 == 0) {
                if (_getTokenTick > 1) {
                    LogHelper.toast("获取token超时，重试 ");
                }
                toGetBindingTokenId();
                bindChangyoyoPlatform(phoneNum, servicePassword, smsCode, callback);
                return;
            }
            if (_getTokenTick > 200) {
                callback.callback(23, "获取token超时");
                return;
            }
            // 如果未加载完成，则等待500毫秒再调
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    bindChangyoyoPlatform(phoneNum, servicePassword, smsCode, callback);
                }
            }, 500);
            return;
        }
        final String tokenId = _bindingTokenId;
        String pwd = convertPassword(servicePassword);
        LogHelper.d("bindChangyoyoPlatform " + phoneNum + " " + pwd + " " + smsCode + " " + tokenId);
        if (StringHelper.isEmpty(pwd)) {
            callback.callback(1, "密码是空的");
            return;
        }
//        NetHelper.getInstance().doWebBinding(phoneNum, pwd, smsCode, callback);
        Map<String, String> params = DataHelper.toMap(
                "tokenId", tokenId,
                "interCode", "JF0001-1",
                "partnerId", "2100",
                "pwd", pwd,
                "mobile", phoneNum,
                "smsCode", smsCode
        );
        String url = "https://open.jf.10086.cn/bind/bindInfo.service";
//        NetHelper.getInstance().postInWebView(url, params);
        NetHelper.getInstance().postDataAsynToNetForBinding(url, params, callback);
//                new NetHelper.MyNetCall() {
//                    @Override
//                    public void success(Call call, Response response) throws IOException {
//                        callback.callback(0, response.body().string());
//                    }
//
//                    @Override
//                    public void failed(Call call, IOException e) {
//                        callback.callback(1, "{}");
//                    }
//                });
    }

    String getDefaultEmail() {
        return "nil@qq.com";
    }

    public String getDefaultPassword() {
        return "abcd1234";
    }

    void setIdCardNum(String idCardNum) {
        _idCardNum = idCardNum;
    }

    public String getIdCardNum() {
        return _idCardNum;
    }
}
