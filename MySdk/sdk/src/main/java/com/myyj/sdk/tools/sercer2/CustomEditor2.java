package com.myyj.sdk.tools.sercer2;

import com.myyj.sdk.tools.JsonHelper;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class CustomEditor2 {
    private ServerHelper2 serverHelper;
    private JSONObject dataCurrent;
    private JSONObject dataSaved;

    public CustomEditor2(ServerHelper2 serverHelper) {
        this.serverHelper = serverHelper;
        LogHelper.d("new CustomEditor");
    }

    public void init() {
        String json = serverHelper.getCustom();
        try {
            if(StringHelper.isEmpty(json)) {
                json = "{}";
            }
            dataCurrent = new JSONObject(json);
            dataSaved = new JSONObject(dataCurrent.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(dataCurrent == null) {
                dataCurrent = new JSONObject();
                dataSaved = new JSONObject();
            }
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(needCommit) {
                    commitInner();
                }
            }
        }, 0, 100);
        LogHelper.d("editor.init " + dataCurrent.toString());
    }
    private boolean needCommit;
    /**
     * 注意，整个json不可超过255个字节
     */
    public void commit() {
        LogHelper.d("editor.commit");
        needCommit = true;
    }

    public void commitInner() {
        if(!JsonHelper.areEqual(dataCurrent, dataSaved)) {
            String data = dataCurrent.toString();
            LogHelper.d("commitInner " + data);
            try {
                if(data.length() >= 255) {
                    LogHelper.toast("警告！存档超长！" + data.length());
                }
                dataSaved = new JSONObject(data);
                serverHelper.updateCustom(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            LogHelper.d("Data not changed.");
        }
        needCommit = false;
    }

    public int getInt(String key, int defaultValue) {
        return dataCurrent.optInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        try {
            dataCurrent.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key, String defaultValue) {
        return dataCurrent.optString(key, defaultValue);
    }

    public void putString(String key, String value) {
        try {
            dataCurrent.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return dataCurrent.optBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        try {
            dataCurrent.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double getDouble(String key, double defaultValue) {
        return dataCurrent.optDouble(key, defaultValue);
    }

    public void putDouble(String key, double value) {
        try {
            dataCurrent.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
