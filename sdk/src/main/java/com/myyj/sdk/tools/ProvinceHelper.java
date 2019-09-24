package com.myyj.sdk.tools;

import android.app.Activity;

import com.qw.soul.permission.SoulPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ProvinceHelper {
    private static String[] PROVS = {"","北京", "上海", "广东", "天津", "重庆", "浙江", "江苏", "黑龙江", "吉林",
            "辽宁", "内蒙古", "河南", "山东", "河北", "山西", "四川", "安徽", "湖北", "福建", "江西",
            "广西", "海南", "云南", "宁夏", "甘肃", "新疆", "西藏", "青海", "贵州", "湖南", "陕西"};
    private static String[] PRES = {"134", "135", "136", "137", "138", "139", "147",
            "150", "151", "152", "157", "158", "159", "178", "182", "183", "184", "187", "188"};


    private static byte[] buffer;

    private static ProvinceHelper instance;

    public static ProvinceHelper getInstance() {
        if (instance == null) {
            instance = new ProvinceHelper();
        }
        return instance;
    }


    public static void initBin() {
        if (buffer != null) {
            return;
        }
        try {
//            File file = new File("dp.bin");
            InputStream abpath = getInstance().getClass().getResourceAsStream("/assets/dp.bin");
//            FileInputStream fis = new FileInputStream(file);
            int count = PRES.length * 10000;
            buffer = new byte[count];
            abpath.read(buffer, 0, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dispose() {
        buffer = null;
    }

    public static String getProvince(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return null;
        }
        if (buffer == null) {
            initBin();
        }
        String pre = phoneNumber.substring(0, 3);
        String center = phoneNumber.substring(3, 7);
        List<String> listProv = Arrays.asList(PRES);
        int index = listProv.indexOf(pre);
        if (index < 0) {
            return null;
        }
        index = index * 10000;
        index = index + Integer.valueOf(center);
        byte prov = buffer[index];

        if(prov == 0)
        {
            SuperSmsManager.PhoneInfo info = SuperSmsManager.getInstance().getPhoneInfo(phoneNumber);
            return info.getProvince();
        }
        return PROVS[prov];
    }

}