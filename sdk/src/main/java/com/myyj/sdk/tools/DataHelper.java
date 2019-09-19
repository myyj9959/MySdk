package com.myyj.sdk.tools;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class DataHelper extends BaseHelper {

    /************************* 基础方法 *******************************/
    public static Map<String, String> toMap(String... args) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }

    public static String fromMap(Map<String, String> map) {
        if (null == map) {
            return null;
        }
        JSONObject o = new JSONObject();
        for (Map.Entry<String, String> e : map.entrySet()) {
            try {
                o.put(e.getKey(), e.getValue());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return o.toString();
    }

    public static String getMD5(String string) {
        if (StringHelper.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result.toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

//    /************************* 本地数据 *******************************/
//    private final static String STORE_KEY = "YOSHI_STORE";
//
//    private static SharedPreferences sp;
//
//    public static SharedPreferences getStore() {
//        if (sp == null) {
//            sp = getActivity().getSharedPreferences(STORE_KEY, 0);
//        }
//        return sp;
//    }
//
//    public static boolean saveIntLocal(String key, int value) {
//        return getStore().edit().putInt(key, value).commit();
//    }
//
//    public static Integer readIntLocal(String key) {
//        return getStore().getInt(key, 0);
//    }
//
//    public static boolean saveFloatLocal(String key, float value) {
//        return getStore().edit().putFloat(key, value).commit();
//    }
//
//    public static Float readFloatLocal(String key) {
//        return getStore().getFloat(key, 0);
//    }
//
//    public static boolean saveStringLocal(String key, String value) {
//        return getStore().edit().putString(key, value).commit();
//    }
//
//    public static String readStringLocal(String key) {
//        return getStore().getString(key, "");
//    }
}