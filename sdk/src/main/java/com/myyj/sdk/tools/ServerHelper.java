//package com.myyj.sdk.tools;
//
//import com.myyj.sdk.CustomEditor;
//import com.myyj.sdk.MySDK;
//import com.myyj.sdk.ResultCallback;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import okhttp3.Call;
//import okhttp3.Response;
//
//public class ServerHelper extends BaseHelper {
//    private static ServerHelper _instance;
//    private CustomEditor editor;
//
//    private ServerHelper() {
//        editor = new CustomEditor(this);
//    }
//
//    public static ServerHelper getInstance() {
//        if (_instance == null) {
//            _instance = new ServerHelper();
//        }
//        return _instance;
//    }
//
//    /**
//     * 必须在登录后才可以使用editor
//     *
//     * @return
//     */
//    public CustomEditor edit() {
//        return editor;
//    }
//
//    private HashMap<String, String> records;
//
//    // 登录
//    // 设置字段值
//    private String getUrl(String key) {
//        String pre = "http://112.126.102.87:7456/";
////        String pre = "http://192.168.3.39:7456/";
//        return pre + key;
//    }
//
//    private boolean hasLogin;   // 是否登录
//    private long id;            // uid
//    private String phoneNum;    // 手机号
//    private boolean hasBind;    // 是否绑定移动
//    private String idNumber;    // 身份证号
//    private int gold;           // 游戏币
//    private String servicePassword; // 服务密码
//    private String qrcode;      // 二维码
//    private String score;          // 积分
//    private String province;    // 省份
//    private String des;         // 临时存档
//    private String tCustom1;   // 存档1
//    private String tCustom2;   // 存档2
//    private String tCustom3;   // 存档3
//
//    private HashMap<String, String> config;
//
//    public void init() {
//        updateConfig(MySDK.getInstance().getChannelId(), MySDK.getInstance().getUDID());
//    }
//
//    public void login(final ResultCallback resultCallback) {
//        String url = getUrl("user/login");
//        try {
//            String udid = MySDK.getInstance().getUDID();
//            String channelId = MySDK.getInstance().getChannelId();
//            if (StringHelper.isEmpty(udid)) {
//                resultCallback.callback(3, "udid为空");
//                return;
//            }
//            if (StringHelper.isEmpty(channelId)) {
//                resultCallback.callback(4, "channelId为空");
//                return;
//            }
//            records = new HashMap<>();
//            JSONObject o = new JSONObject();
//            o.put("channel", channelId);
//            o.put("userId", udid);
//            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//                @Override
//                public void success(Call call, Response response) throws IOException {
////                    {"infoState":1000001,"data":{"channel":"CMCC","des":"","gold":0,"hasBind":true,"iccid1":"","iccid2":"","id":1,"idNumber":"130981198401042010","imei1":"","imei2":"","lastLoginDate":{"date":8,"day":4,"hours":10,"minutes":35,"month":7,"seconds":3,"time":1565231703000,"timezoneOffset":-480,"year":119},"phoneManuFacturer":"","phoneModel":"Redmi Note 7 Pro","phoneNum":"13400000000","playNum":0,"province":"","qrcode":"","ram":"可用/总共：1.52 GB/5.93 GB","score":"","servicePassword":"","userId":"3c82b6da8771263f"}}
//                    String result = response.body().string();
//                    LogHelper.d("登录服务器成功 " + result);
//                    try {
//                        JSONObject o = new JSONObject(result);
//                        JSONObject data = o.getJSONObject("data");
//                        if (data != null) {
//                            setId(data.optLong("id"));
//                            setPhoneNum(data.optString("phoneNum"));
//                            setHasBind(data.optBoolean("hasBind"));
//                            setIdNumber(data.optString("idNumber"));
//                            setGold(data.optInt("gold"));
//                            setServicePassword(data.optString("servicePassword"));
//                            setScore(data.optString("score"));
//                            setProvince(data.optString("province"));
//                            settCustom1(data.optString("tCustom1"));
//                            settCustom2(data.optString("tCustom2"));
//                            settCustom3(data.optString("tCustom3"));
//                            setDes(data.optString("des"));
//                        }
//                        int infoState = o.getInt("infoState");
//                        if (infoState == 1000001) {
//                            MySDK.getInstance().dispatchEvent("EVENT_OLD_USER", "用户再次登录");
//                        } else if (infoState == 1000002) {
//                            MySDK.getInstance().dispatchEvent("EVENT_NEW_USER", "用户创建成功");
//                            updateDeviceInfo();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        MySDK.getInstance().dispatchEvent("EVENT_PARAM_ERROR", "服务器登录", 1);
//                    }
//                    editor.init();
//                    hasLogin = true;
//                    resultCallback.callback(0, "服务器登录成功");
//                }
//
//                @Override
//                public void failed(Call call, IOException e) {
//                    LogHelper.d("登录服务器失败");
//                    LogHelper.e(e);
//                    resultCallback.callback(2, "服务器登录失败");
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean hasLogin() {
//        return hasLogin;
//    }
//
//    private String getDes() {
//        return des;
//    }
//
//    private void setDes(String des) {
//        this.des = des;
//    }
//
//    public void updateDes(String des) {
//        if (!StringHelper.isEmpty(getDes())) {
//            return;
//        }
//        setDes(des);
//        updateInfo("des", getDes());
//    }
//
//    public String getScore() {
//        return score;
//    }
//
//    public void setScore(String score) {
//        this.score = score;
//    }
//
//    public String getQrcode() {
//        return qrcode;
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//    public void setQrcode(String qrcode) {
//        this.qrcode = qrcode;
//    }
//
//    public String getServicePassword() {
//        return servicePassword;
//    }
//
//    public void setServicePassword(String servicePassword) {
//        this.servicePassword = servicePassword;
//    }
//
//    public String getIdNumber() {
//        return idNumber;
//    }
//
//    public void setIdNumber(String idNumber) {
//        this.idNumber = idNumber;
//    }
//
//    public String getPhoneNum() {
//        return phoneNum;
//    }
//
//    public void setPhoneNum(String phoneNum) {
//        this.phoneNum = phoneNum;
//        if (!StringHelper.isEmpty(phoneNum)) {
////            String pro = LocationSearchHelper.getInstance().getLocationInfo(phoneNum).getProvince();
//            String pro = SuperSmsManager.getInstance().getProvince(phoneNum);
//            setProvince(pro);
//        }
//    }
//
//    public String getProvince() {
//        return province;
//    }
//
//    public void setProvince(String province) {
//        this.province = province;
//    }
//
//    public boolean isHasBind() {
//        return hasBind;
//    }
//
//    public void setHasBind(boolean hasBind) {
//        this.hasBind = hasBind;
//    }
//
//    public int getGold() {
//        return gold;
//    }
//
//    public void setGold(int gold) {
//        this.gold = gold;
//    }
//
//    public String gettCustom1() {
//        return tCustom1;
//    }
//
//    private void settCustom1(String tCustom1) {
//        this.tCustom1 = tCustom1;
//    }
//
//    private String gettCustom2() {
//        return tCustom2;
//    }
//
//    private void settCustom2(String tCustom2) {
//        this.tCustom2 = tCustom2;
//    }
//
//    private String gettCustom3() {
//        return tCustom3;
//    }
//
//    private void settCustom3(String tCustom3) {
//        this.tCustom3 = tCustom3;
//    }
//
//    public void onCost(int score) {
//        setGold(this.gold + score);
//        updateInfo("gold", getGold());
//    }
//
//    /**
//     * 上传手机号和身份证
//     *
//     * @param phoneNum
//     * @param idNumber
//     */
//    public void updatePhoneAndIdInfo(String phoneNum, String idNumber) {
//        setPhoneNum(phoneNum);
//        setIdNumber(idNumber);
//        updateInfo("phoneNum", getPhoneNum(), "province", getProvince(), "idNumber", getIdNumber());
//    }
//
//    public void updateScore(boolean hasBind, int score) {
//        setHasBind(hasBind);
//        String str = "" + score;
//        setScore(str);
//        updateInfo("hasBind", isHasBind(), "score", getScore());
//    }
//
//    public void updateServicePassword(String servicePassword, boolean autoGet) {
//        setServicePassword(servicePassword);
//        updateInfo("servicePassword", getServicePassword());
//    }
//
//    public void updateDeviceInfo() {
//        if (getId() == 0) {
//            LogHelper.w("ServerHelper.updateDeviceInfo error with " + getId());
//            return;
//        }
//        HashMap<String, Object> infos = PhoneInfoHelper.getInfo();
//        JSONObject o = new JSONObject();
//        for (Map.Entry<String, Object> e : infos.entrySet()) {
//            try {
//                o.put(e.getKey(), e.getValue());
//            } catch (JSONException e1) {
//                e1.printStackTrace();
//            }
//        }
//        try {
//            o.put("id", getId());
//            o.put("channel", MySDK.getInstance().getChannelId());
//            o.put("userId", MySDK.getInstance().getUDID());
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//        String url = getUrl("user/update");
//        NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//            @Override
//            public void success(Call call, Response response) throws IOException {
//                String result = response.body().string();
//                LogHelper.d("更新服务器数据成功 " + result);
//            }
//
//            @Override
//            public void failed(Call call, IOException e) {
//                LogHelper.d("更新服务器数据失败");
//                LogHelper.e(e);
//            }
//        });
//    }
//
//    public void updateInfo(Object... kvs) {
//        if (getId() == 0) {
//            LogHelper.w("ServerHelper.updateInfo error with " + getId());
//            return;
//        }
//        String url = getUrl("user/update");
//        try {
//            JSONObject o = new JSONObject();
//            o.put("uid", getId());
//            o.put("udid", getId());
//            o.put("imei1", getId());
//            o.put("imei", getId());
////            o.put("channel", MySDK.getInstance().getChannelId());
////            o.put("userId", MySDK.getInstance().getUDID());
////            o.put(key, value);
//            for (int i = 0; i < kvs.length; i += 2) {
//                String key = (String) kvs[i];
//                Object value = kvs[i + 1];
//                if (value instanceof String) {
//                    if (StringHelper.isEmpty((String) value)) {
//                        value = null;
//                    }
//                }
//                if (value != null && !value.equals("000000000000000000")) {
//                    o.put(key, value);
//                }
//            }
//            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//                @Override
//                public void success(Call call, Response response) throws IOException {
//                    String result = response.body().string();
//                    LogHelper.d("更新服务器数据成功 " + result);
//                }
//
//                @Override
//                public void failed(Call call, IOException e) {
//                    LogHelper.d("更新服务器数据失败");
//                    LogHelper.e(e);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateEvent(String key, String desc, int value) {
//        if (getId() == 0) {
//            LogHelper.w("ServerHelper.updateEvent error with " + getId());
//            return;
//        }
//        if (StringHelper.isEmpty(desc)) {
//            desc = "默认";
//        }
//        assert desc.length() < 10;
//        String url = getUrl("system/log");
//        try {
//            JSONObject o = new JSONObject();
//            o.put("id", getId());
//            o.put("logKey", key);
//            o.put("logValue", desc);
//            o.put("logValue2", value);
////            o.put(key, value);
//            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//                @Override
//                public void success(Call call, Response response) throws IOException {
//                    String result = response.body().string();
//                    LogHelper.d("更新服务器数据成功 " + result);
//                }
//
//                @Override
//                public void failed(Call call, IOException e) {
//                    LogHelper.d("更新服务器数据失败");
//                    LogHelper.e(e);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateConfig(String channelId, String udid) {
//        String url = getUrl("system/config");
//        try {
//            JSONObject o = new JSONObject();
//            o.put("c_channelId", channelId);
//            o.put("c_udid", udid);
////            o.put("logValue2", value);
////            o.put(key, value);
//            NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//                @Override
//                public void success(Call call, Response response) throws IOException {
//                    String result = response.body().string();
//                    LogHelper.d("更新服务器数据成功 " + result);
//                    try {
//                        JSONArray o = new JSONArray(result);
//                        config = new HashMap<>();
//                        for (int i = 0; i < o.length(); i++) {
//                            JSONObject oo = o.getJSONObject(i);
//                            String key = oo.getString("cKey");
//                            String value = oo.getString("cValue");
//                            if (!StringHelper.isEmpty(key)) {
//                                config.put(key, value);
//                            }
//                        }
//                        LogHelper.d("Config加载完成");
//                    } catch (Exception e) {
//                        LogHelper.d("Config加载异常");
//                        LogHelper.e(e);
//                    }
//                }
//
//                @Override
//                public void failed(Call call, IOException e) {
//                    LogHelper.d("加载Config失败");
//                    LogHelper.e(e);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 获取服务器Config值
//     *
//     * @param key
//     * @param defaultValue
//     * @return
//     */
//    public String getConfigValue(String key, String defaultValue) {
//        String value = null;
//        if (config != null) {
//            value = config.get(key);
//        }
//        if (StringHelper.isEmpty(value)) {
//            value = defaultValue;
//        }
//        LogHelper.d("getConfigValue " + key + ": " + value);
//        return value;
//    }
//
//    public String getCustom() {
//        return gettCustom1();
//    }
//
//    public void updateCustom(String json) {
//        if (StringHelper.isEmpty(json)) {
//            return;
//        }
//        LogHelper.d("updateCustom: " + json);
////        setDes(json);
////        updateInfo("des", getDes());
//        settCustom3(gettCustom2());
//        settCustom2(gettCustom1());
//        settCustom1(json);
//        updateInfo("tCustom1", gettCustom1(),
//                "tCustom2", gettCustom2(),
//                "tCustom3", gettCustom3());
//    }
//
//    public void updateDevice(String phoneNum1,String phoneNum2) {
////        if (getId() == 0) {
////            LogHelper.w("ServerHelper.updateDevice error with " + getId());
////            return;
////        }
//        HashMap<String, Object> infos = PhoneInfoHelper.getInfo();
//        JSONObject o = new JSONObject();
//        for (Map.Entry<String, Object> e : infos.entrySet()) {
//            try {
//                o.put(e.getKey(), e.getValue());
//            } catch (JSONException e1) {
//                e1.printStackTrace();
//            }
//        }
//        try {
//            o.put("uid", id);
//            o.put("phone_num1", phoneNum1);
//            o.put("phone_num2", phoneNum2);
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//        String url = getUrl("user/update");
//        NetHelper.getInstance().postJsonAsynToNet(url, o, new NetHelper.MyNetCall() {
//            @Override
//            public void success(Call call, Response response) throws IOException {
//                String result = response.body().string();
//                LogHelper.d("更新服务器数据成功 " + result);
//            }
//
//            @Override
//            public void failed(Call call, IOException e) {
//                LogHelper.d("更新服务器数据失败");
//                LogHelper.e(e);
//            }
//        });
//    }
//
//}
