package com.myyj.sdk.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JsonHelper {
    public static boolean areEqual(Object ob1, Object ob2) {
        Object obj1Converted = convertJsonElement(ob1);
        Object obj2Converted = convertJsonElement(ob2);
        if(obj1Converted == null || obj2Converted == null) {
            return false;
        }
        return obj1Converted.equals(obj2Converted);
    }

    private static Object convertJsonElement(Object elem) {
        try {
            if (elem instanceof JSONObject) {
                JSONObject obj = (JSONObject) elem;
                Iterator<String> keys = obj.keys();
                Map<String, Object> jsonMap = new HashMap<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    jsonMap.put(key, convertJsonElement(obj.get(key)));
                }
                return jsonMap;
            } else if (elem instanceof JSONArray) {
                JSONArray arr = (JSONArray) elem;
                Set<Object> jsonSet = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) {
                    jsonSet.add(convertJsonElement(arr.get(i)));
                }
                return jsonSet;
            } else {
                return elem;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
