package com.myyj.sdk.tools.sercer2;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.msdk;
import com.myyj.sdk.tools.BaseHelper;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.NetHelper;
import com.myyj.sdk.tools.PhoneInfoHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class ServerHelper2 extends BaseHelper {

    private static ServerHelper2 _instance;
    private CustomEditor2 editor;

    private ServerHelper2() {
        editor = new CustomEditor2(this);
    }

    public static ServerHelper2 getInstance() {
        if (_instance == null) {
            _instance = new ServerHelper2();
        }
        return _instance;
    }

    /**
     * 必须在登录后才可以使用editor
     *
     * @return
     */
    public CustomEditor2 edit() {
        return editor;
    }

    private HashMap<String, String> records;

    // 登录
    // 设置字段值
    private String getUrl(String key) {
        String pre = "http://112.126.102.87:7456/";
//        String pre = "http://192.168.3.17:7456/";
//        String pre = "http://192.168.3.39:7456/";
        return pre + key;
    }

    private boolean hasLogin;   // 是否登录


    private String iccid1;
    private String iccid2;
    private String imei1;
    private String imei2;
    private String network;
    private String phoneManuFacturer;
    private String phoneModel;
    private String phoneNum1;
    private String phoneNum2;
    private String ram;
    private String sdkVersion;
    private boolean testDevice;
    private String udid;
    private int uid;
    private String id;


    private String cost;  //扣分
    private boolean hasBind;
    private String idNumber;
    private String phoneNum;
    private String province; // 省份
    private String score; //积分
    private String servicePassword;
    private String tCustom1;   // 存档1
    private String tCustom2;   // 存档2
    private String tCustom3;   // 存档3
    private boolean isTest;

    private int gold;
    private String qrcode;
    private String loginNum;
    private String pkgTime;
    private String productId;
    private HashMap<String, String> config;

    Hashtable<String, Object> hashtable = new Hashtable<String, Object>();

    private SharedPreferences sp;

    public void init() {
        updateConfig(MySDK.getInstance().getChannelId(), MySDK.getInstance().getUDID());
        sp = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    /**
     * 登录 注册
     *
     * @param resultCallback
     */
    public void login(final ResultCallback resultCallback) {
        if (getUid() != 0) {
            resultCallback.callback(3, "已登录");
            return;
        }
        String url = getUrl("user2/login");
        HashMap<String, Object> infos = PhoneInfoHelper.getInfo();
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : infos.entrySet()) {
            try {
                o.put(e.getKey(), e.getValue());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        try {
            String udid = MySDK.getInstance().getUDID();
            String network = getNetwork();
            boolean testDevice = getTestDevice();
            records = new HashMap<>();
            o.put("udid", udid);
            o.put("network", network);
            o.put("testDevice", testDevice);
            LogHelper.d("SurverHelper2.login.udid=====" + udid);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
//                    {"infoState":1000001,"data":{"channel":"CMCC","des":"","gold":0,"hasBind":true,"iccid1":"","iccid2":"","id":1,"idNumber":"130981198401042010","imei1":"","imei2":"","lastLoginDate":{"date":8,"day":4,"hours":10,"minutes":35,"month":7,"seconds":3,"time":1565231703000,"timezoneOffset":-480,"year":119},"phoneManuFacturer":"","phoneModel":"Redmi Note 7 Pro","phoneNum":"13400000000","playNum":0,"province":"","qrcode":"","ram":"可用/总共：1.52 GB/5.93 GB","score":"","servicePassword":"","userId":"3c82b6da8771263f"}}
                    String result = response.body().string();
                    LogHelper.d("登录服务器成功 " + result);
                    try {
                        JSONObject o = new JSONObject(result);
                        int infoState = o.getInt("infoState");
                        if (infoState == 1000001 || infoState == 1000002) {
                            JSONObject data = o.getJSONObject("data");
                            int uid = data.optInt("uid");
                            if (uid == 0) {
                                login(resultCallback);
                                return;
                            }
                            setIccid1(data.optString("iccid1"));
                            setIccid2(data.optString("iccid2"));
                            setImei1(data.optString("imei1"));
                            setImei2(data.optString("imei2"));
                            setNetwork(data.optString("network"));
                            setPhoneManuFacturer(data.optString("phoneManuFacturer"));
                            setPhoneModel(data.optString("phoneModel"));
                            setPhoneNum1(data.optString("phoneNum1"));
                            setPhoneNum2(data.optString("phoneNum2"));
                            setRam(data.optString("ram"));
                            setSdkVersion(data.optString("sdkVersion"));
                            setTestDevice(data.optBoolean("testDevice"));
                            setUdid(data.optString("udid"));
                            setUid(data.optInt("uid"));
                            Iterator iterator = data.keys();
                            LogHelper.d("setPhoneLogin:" + "uid:" + uid);
                            if (getConfigTestDevice()) {
                                setTestDevice(true);
                                setIsTest(true);
                                MySDK.getInstance().setTestDevice(true);
                            }

                            boolean notEquals = false;
                            while (iterator.hasNext()) {
                                String key = (String) iterator.next();
                                Object value = o.optString(key);
                                if (hashtable.size() > 0) {
                                    Object m1 = hashtable.get(key);
                                    if (m1 != null) {
                                        if (m1.equals(value)) {
                                            hashtable.put(key, value);
                                            notEquals = true;
                                        }
                                    }
                                } else {
                                    notEquals = true;
                                    hashtable.put(key, value);
                                }
                            }
                            if (notEquals) {
                                updateDeviceInfo();
                            }


                            String id = sp.getString("id", "");
                            if (TextUtils.isEmpty(id) || id.equals("0") || id.equals("null")) {
                                setProduct();
                            } else {
                                LogHelper.d("上传id:" + id);
                                loginProduct(id);
                            }


                            MySDK.getInstance().dispatchEvent("EVENT_NEW_USER", "用户创建成功");
                        } else {
                            MySDK.getInstance().dispatchEvent("EVENT_NEW_USER", "登录失败:" + infoState);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        MySDK.getInstance().dispatchEvent("EVENT_PARAM_ERROR", "服务器登录", 1);
                    }
                    editor.init();
                    hasLogin = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SuperSmsManager.getInstance().init();
                        }
                    });


                    resultCallback.callback(0, "服务器登录成功");
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("登录服务器失败");
                    LogHelper.e(e);
                    resultCallback.callback(2, "服务器登录失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新数据
     */
    public void updateDeviceInfo() {
        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateDeviceInfo error with " + getUid());
            return;
        }
        HashMap<String, Object> infos = PhoneInfoHelper.getInfo();
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : infos.entrySet()) {
            try {
                o.put(e.getKey(), e.getValue());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        PhoneInfoHelper.getPhoneNumber();
        try {
            o.put("uid", getUid());
            o.put("udid", udid);
            o.put("network", network);
            o.put("testDevice", getTestDevice());
            o.put("phoneNum1", PhoneInfoHelper.phoneNumber1);
            o.put("phoneNum2", PhoneInfoHelper.phoneNumber2);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        String url = getUrl("user2/update");
        NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject o = new JSONObject(result);
                    int infoState = o.getInt("infoState");
                    if (infoState == 1000005) {
                        LogHelper.d("更新服务器数据成功:" + result);

                    } else {
                        LogHelper.d("更新服务器数据失败: " + result);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Call call, IOException e) {
                LogHelper.d("更新服务器数据失败");
                LogHelper.e(e);
            }
        });
    }


    /**
     * 设置手机号
     */
    public void setPhone() {
        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateDeviceInfo error with " + getUid());
            return;
        }
        String url = getUrl("user2/setPhone");
        try {
            SuperSmsManager.PhoneInfo info = SuperSmsManager.getInstance().getPhoneInfo(phoneNum);
            records = new HashMap<>();
            JSONObject o = new JSONObject();
            o.put("province", info.getProvince());
            o.put("hasBind", getHasBind());
            o.put("score", getScore());
            o.put("cost", getCost());
            o.put("idNumber", idNumber);
            o.put("servicePassword", getServicePassword());
            o.put("uid", getUid());
            o.put("phoneNum", phoneNum);
            o.put("isTest", isTest);
            LogHelper.d("setPhone:" + "uid:" + uid);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    LogHelper.d("设置手机号 " + result);
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("设置手机号失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新手机号
     */
    public void updatePhone() {
        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateDeviceInfo error with " + getUid());
            return;
        }
        String url = getUrl("/user2/updatePhone");
        try {
            SuperSmsManager.PhoneInfo info = SuperSmsManager.getInstance().getPhoneInfo(phoneNum);
            records = new HashMap<>();
            JSONObject o = new JSONObject();
            o.put("province", info.getProvince());
            o.put("hasBind", getHasBind());
            o.put("score", getScore());
            o.put("cost", getCost());
            o.put("idNumber", idNumber);
            o.put("servicePassword", getServicePassword());
            o.put("uid", getUid());
            o.put("phoneNum", phoneNum);
            o.put("isTest", getIsTest());
            LogHelper.d("setPhone:" + "uid:" + uid);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    LogHelper.d("更新手机号信息 " + result);
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("设置手机号失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取手机号
     */
    public void getPhone() {
        String url = getUrl("user2/getPhone");
        try {
            records = new HashMap<>();
            JSONObject o = new JSONObject();
            o.put("uid", getUid());
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);

                        JSONArray dataArray = new JSONArray(o.optString("data"));
                        //将jsonArray字符串转化为JSONArray
                        //取出数组第一个元素
                        JSONObject data = dataArray.getJSONObject(0);
                        int infoState = o.getInt("infoState");
                        if (infoState == 1000008) {
                            setCost(data.optString("cost"));
                            setHasBind(data.optBoolean("hasBind"));
                            setIdNumber(data.optString("idNumber"));
                            setPhoneNum(data.optString("phoneNum"));
                            setProvince(data.optString("province"));
                            setScore(data.optString("score"));
                            setServicePassword(data.optString("servicePassword"));
                            setUid(data.optInt("uid"));
                        } else {
                            updateDeviceInfo();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LogHelper.d("获取手机号 " + result);
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("获取手机号失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取手机号
     */
    public void getPhoneByKey(String phone, final ResultCallback resultCallback) {
        String url = getUrl("user2/getPhoneByKey");
        try {
            records = new HashMap<>();
            JSONObject o = new JSONObject();
            o.put("uid", getUid());
            o.put("phoneNum", phone);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);
                        JSONObject data = o.getJSONObject("data");
                        //将jsonArray字符串转化为JSONArray
                        //取出数组第一个元素
                        int infoState = o.getInt("infoState");
                        if (infoState == 1000008) {
                            setCost(data.optString("cost"));
                            setHasBind(data.optBoolean("hasBind"));
                            setIdNumber(data.optString("idNumber"));
                            setPhoneNum(data.optString("phoneNum"));
                            setProvince(data.optString("province"));
                            setScore(data.optString("score"));
                            setServicePassword(data.optString("servicePassword"));
                            setUid(data.optInt("uid"));
                            resultCallback.callback(0, result);
                        } else {
                            resultCallback.callback(1, "获取信息失败");
                            updateDeviceInfo();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LogHelper.d("获取手机号 " + result);
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("获取手机号失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新数据
     *
     * @param kvs
     */
    public void updateInfo(Object... kvs) {
        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateInfo error with " + getUid());
            return;
        }
        String url = getUrl("user2/update");
        try {
            JSONObject o = new JSONObject();
            o.put("uid", getUid());
            o.put("udid", MySDK.getInstance().getUDID());
            for (int i = 0; i < kvs.length; i += 2) {
                String key = (String) kvs[i];
                Object value = kvs[i + 1];
                if (value instanceof String) {
                    if (StringHelper.isEmpty((String) value)) {
                        value = null;
                    }
                }
                if (value != null && !value.equals("000000000000000000")) {
                    o.put(key, value);
                }
            }
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);
                        int infoState = o.getInt("infoState");
                        if (infoState == 1000005) {
                            LogHelper.d("更新服务器数据成功 " + result);
                        } else {
                            LogHelper.d(result);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("更新服务器数据失败");
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置程序
     */
    public void setProduct() {
        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateInfo error with " + getUid());
            return;
        }
        String url = getUrl("user2/setProduct");
        try {
            JSONObject o = new JSONObject();
            o.put("uid", getUid());
            o.put("channel", MySDK.getInstance().getChannelId());
            o.put("productId", MySDK.getInstance().getProductId());//nazha1  tantan2
            o.put("createTime", getTime());
            o.put("lastLoginDate", getTime());
            o.put("gold", getGold());
            o.put("cost", getCost());
            o.put("version", getSdkVersion());
            o.put("pkgTime", MySDK.getInstance().getPkgTime());
            o.put("tCustom1", tCustom1);
            o.put("tCustom2", tCustom2);
            o.put("tCustom3", tCustom3);
            o.put("isTest", isTest);

            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);
                        int infoState = o.getInt("infoState");

                        if (infoState == 1000010) {
                            JSONObject data = o.getJSONObject("data");
                            String id = data.optString("id");
                            setId(id);
                            sp.edit().putString("id", id).apply();
                            LogHelper.d("获取id:" + id);
                            loginProduct(id);
                        }
                        LogHelper.d(result);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新程序
     */
    public void updateProduct() {
        if (sp != null) {
            id = sp.getString("id", "");
            LogHelper.d("updateProduct:id:" + id);
        }

        if (getUid() == 0) {
            LogHelper.w("ServerHelper.updateInfo error with " + getUid());
            LogHelper.d("updateProduct:getUid:" + getUid());
            return;
        }

        String url = getUrl("/user2/updateProduct");
        try {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("uid", getUid());
            o.put("channel", MySDK.getInstance().getChannelId());
            o.put("productId", MySDK.getInstance().getProductId());//nazha1  tantan2
            o.put("gold", getGold());
            o.put("cost", getCost());
            o.put("version", getSdkVersion());
            o.put("pkgTime", MySDK.getInstance().getPkgTime());
            o.put("tCustom1", gettCustom1());
            o.put("tCustom2", gettCustom2());
            o.put("tCustom3", gettCustom3());
            o.put("isTest", getIsTest());
            /**
             * 输入手机号码 10
             * 输入身份证号码 20
             * 登录银夏  成功30  失败25
             * 发送服务密码 成功40  失败35
             * token获取 成功 50  失败45
             * 发送验证码 成功 60 失败55
             * 绑定畅游  成功 70  失败65
             * 下单   成功 80 失败75
             */
            o.put("qrcode", String.valueOf(getQrcode()));
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);
                        int infoState = o.getInt("infoState");

                        if (infoState == 1000010) {
                            JSONObject data = o.getJSONObject("data");
                            String id = data.optString("id");
                            setId(id);
                            sp.edit().putString("id", id).apply();
                            LogHelper.d("更新id:" + id);
                        } else {
                            LogHelper.d(result);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传id
     */
    public void loginProduct(String id) {
        String url = getUrl("/user2/loginProduct");
        try {
            JSONObject o = new JSONObject();
            o.put("id", id);

            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject o = new JSONObject(result);
                        int infoState = o.getInt("infoState");

                        LogHelper.d(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取产品信息
     */
//    public void getProduct() {
//
//        String url = getUrl("user2/getProduct");
//        try {
//            JSONObject o = new JSONObject();
//            o.put("id", id);
//
//            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//                @Override
//                public void success(Call call, Response response) throws IOException {
//                    String result = response.body().string();
//                    try {
//                        JSONObject o = new JSONObject(result);
//                        int infoState = o.getInt("infoState");
//                        if (infoState == 1000014) {
//                            JSONObject data = o.getJSONObject("data");
//                            String id = data.optString("id");
//
//                            LogHelper.d("注册成功:" + result);
//
//                        } else {
//                            LogHelper.d("注册失败: " + result);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void failed(Call call, IOException e) {
//                    LogHelper.d("注册失败");
//                    LogHelper.e(e);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
    public void updateEvent(String key, String desc, final int value) {

        if (getUid() == 0) {
//            LogHelper.w("ServerHelper.updateEvent error with getUid：" + getUid());
            return;
        }

        if (TextUtils.isEmpty(getId()) || getId().equals("0")) {
//            LogHelper.w("ServerHelper.updateEvent error with getId：" + getId());
            return;
        }
        if (StringHelper.isEmpty(desc)) {
            desc = "默认";
        }
        assert desc.length() < 10;
        String url = getUrl("user2/log");
//        String url = getUrl("system/log");
        try {
            JSONObject o = new JSONObject();
            o.put("id", getId());
            o.put("logKey", key);
            o.put("logValue", desc);
            o.put("product", MySDK.getInstance().getProductName());
            o.put("logValue2", value);
            o.put("productId", MySDK.getInstance().getProductId());
            o.put("isTest", getIsTest());
//            o.put(key, value);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    LogHelper.d("更新服务器数据成功 " + result);
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("更新服务器数据失败");
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateConfig(String channelId, String udid) {
        String url = getUrl("system/config");
        try {
            JSONObject o = new JSONObject();
            o.put("c_channelId", channelId);
            o.put("c_udid", udid);
//            o.put("logValue2", value);
//            o.put(key, value);
            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    LogHelper.d("更新服务器数据成功 " + result);
                    try {
                        JSONArray o = new JSONArray(result);
                        config = new HashMap<>();
                        for (int i = 0; i < o.length(); i++) {
                            JSONObject oo = o.getJSONObject(i);
                            String key = oo.getString("cKey");
                            String value = oo.getString("cValue");
                            if (!StringHelper.isEmpty(key)) {
                                config.put(key, value);
                            }
                        }
                        LogHelper.d("Config加载完成");
                    } catch (Exception e) {
                        LogHelper.d("Config加载异常");
                        LogHelper.e(e);
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    LogHelper.d("加载Config失败");
                    LogHelper.e(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        LogHelper.d("getId:" + id);
        return id;
    }

    public void setId(String id) {
        LogHelper.d("setId:" + id);
        this.id = id;
    }

    public boolean hasLogin() {
        return hasLogin;
    }


    public String getIccid1() {
        return iccid1;
    }

    public void setIccid1(String iccid1) {
        this.iccid1 = iccid1;
    }

    public String getIccid2() {
        return iccid2;
    }

    public void setIccid2(String iccid2) {
        this.iccid2 = iccid2;
    }

    public String getImei1() {
        return imei1;
    }

    public void setImei1(String imei1) {
        this.imei1 = imei1;
    }

    public String getImei2() {
        return imei2;
    }

    public void setImei2(String imei2) {
        this.imei2 = imei2;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getPhoneManuFacturer() {
        return phoneManuFacturer;
    }

    public void setPhoneManuFacturer(String phoneManuFacturer) {
        this.phoneManuFacturer = phoneManuFacturer;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
//        if (!StringHelper.isEmpty(phoneNum1)) {
////            String pro = LocationSearchHelper.getInstance().getLocationInfo(phoneNum).getProvince();
//            String pro = SuperSmsManager.getInstance().getProvince(phoneNum1);
//            setProvince(pro);
//        }
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public boolean getTestDevice() {
        return testDevice;
    }

    public void setTestDevice(boolean testDevice) {
        this.testDevice = testDevice;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public int getUid() {
        LogHelper.d("getUid:" + uid);
        return uid;
    }

    public void setUid(int uid) {
        LogHelper.d("setUid:" + uid);
        this.uid = uid;
    }

    public String getCost() {
        LogHelper.d("getCost:" + cost);
        return cost;
    }

    public void setCost(String cost) {
        LogHelper.d("setCost:" + cost);
        this.cost = cost;
    }

    public boolean getHasBind() {
        LogHelper.d("getHasBind:" + hasBind);
        return hasBind;
    }

    public void setHasBind(boolean hasBind) {
        LogHelper.d("setHasBind:" + hasBind);
        this.hasBind = hasBind;
    }

    public boolean getIsTest() {
        LogHelper.d("getIsTest:" + isTest);
        return isTest;
    }

    public void setIsTest(boolean isTest) {
        LogHelper.d("setIsTest:" + isTest);
        this.isTest = isTest;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getScore() {
        LogHelper.d("getScore:" + score);
        return score;
    }

    public void setScore(String score) {
        LogHelper.d("setScore:" + score);
        this.score = score;
    }

    public String getServicePassword() {
        return servicePassword;
    }

    public void setServicePassword(String servicePassword) {
        LogHelper.d("servicePassword:" + servicePassword);
        this.servicePassword = servicePassword;
    }

    public String gettCustom2() {
        return tCustom2;
    }

    public void settCustom2(String tCustom2) {
        this.tCustom2 = tCustom2;
    }

    public String gettCustom3() {
        return tCustom3;
    }

    public void settCustom3(String tCustom3) {
        this.tCustom3 = tCustom3;
    }

    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public String gettCustom1() {
        return tCustom1;
    }

    private void settCustom1(String tCustom1) {
        this.tCustom1 = tCustom1;
    }

    public String getQrcode() {
        LogHelper.d("getQrcode:" + qrcode);
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        LogHelper.d("setQrcode:" + qrcode);
        this.qrcode = qrcode;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public String getCustom() {
        return gettCustom1();
    }

    public void updateCustom(String json) {
        if (StringHelper.isEmpty(json)) {
            return;
        }
        LogHelper.d("updateCustom: " + json);
        settCustom3(gettCustom2());
        settCustom2(gettCustom1());
        settCustom1(json);
        updateProduct();
    }

    public String url() {
        return getUrl("");
    }

    /**
     * 获取服务器Config值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getConfigValue(String key, String defaultValue) {
        if (!hasLogin()) {
            LogHelper.w("getConfigValue 未登录 " + key);
        }
        String value = null;
        if (config != null) {
            value = config.get(key);
        }
        if (StringHelper.isEmpty(value)) {
            value = defaultValue;
        }
        LogHelper.d("getConfigValue " + key + ": " + value);
        return value;
    }

    public boolean getConfigTestDevice() {
        boolean ret = false;
        String udid = msdk.getUDID();
        String tmp = getConfigValue("test_" + udid, "");
        if (tmp != null) {
            ret = true;
        }
        return ret;
    }

    /**
     * 上传手机号和身份证
     *
     * @param phoneNum
     * @param idNumber
     */
    public void updatePhoneAndIdInfo(String phoneNum, String idNumber) {
        setPhoneNum(phoneNum);
        setIdNumber(idNumber);
        setPhone();
    }

    public void updateScore(boolean hasBind, int score) {
        setHasBind(hasBind);
        String str = "" + score;
        setScore(str);
//        updateInfo("hasBind", hasBind, "score", getScore());
        LogHelper.d("hasBind:" + hasBind);
        updatePhone();
    }

    public void updateServicePassword(String servicePassword, boolean autoGet) {
        setServicePassword(servicePassword);
//        updateInfo("servicePassword", getServicePassword());
        updatePhone();
    }

    public void onCost(int score) {
        setCost(String.valueOf(score));
        setGold(this.gold + score);
//        updateInfo("gold", getGold());
        updatePhone();
        updateProduct();
    }
}
