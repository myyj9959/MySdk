package com.myyj.sdk.tools;

import android.content.Intent;

import com.myyj.sdk.MySDK;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    /**
     * 判断字符串是否为空
     *
     * @param str 源字符串
     * @return
     */
    public static boolean isEmpty(String str) {
        return "".equals(maskNull(str));
    }

    /**
     * 返回非null字符串
     *
     * @param str 源字符串
     * @return
     */
    public static String maskNull(String str) {
        return (null == str ? "" : str);
    }

    /**
     * 去掉前后的字串，只保留中间的数字
     * @param str
     * @param pre
     * @param last
     * @return
     */
    public static Integer trimForNumber(String str, String pre, String last) {
        if(StringHelper.isEmpty(str) || StringHelper.isEmpty(pre) || StringHelper.isEmpty(last)) {
            return null;
        }
        if(!str.contains(pre) || !str.contains(last)) {
            return null;
        }
        int index = str.indexOf(pre);
        if(index < 0) {
            return null;
        }
        index = index + pre.length();
        int index2 = str.indexOf(last, index + 1);
        if(index2 < 0 || index2 <= index) {
            return null;
        }
        String tmp = str.substring(index, index2);
        Integer ret = null;
        if(isNumeric(tmp)) {
            ret = Integer.valueOf(tmp);
        }
        return ret;
    }

    public static Integer trimForNumber(String str, String... list) {
        if(StringHelper.isEmpty(str) || (list.length % 2 != 0)) {
            LogHelper.w("trimForNumber 参数错误");
            return null;
        }
        for(int i=0;i<list.length;i+=2) {
            String pre = list[i];
            String last = list[i + 1];
            Integer ret = trimForNumber(str, pre, last);
            if(ret != null) {
                return ret;
            }
        }
        return null;
    }

    /**
     * 检查是否全部为数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if(null == str) {
            return false;
        }
        str = str.trim();
        if(str.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static String checkBitsCode(String body, int bitCount) {
        String result = "";
        Pattern pattern = Pattern.compile("(\\d{"+bitCount+"})");//提取六位数字
        Matcher matcher = pattern.matcher(body);//进行匹配
        while(matcher.find()){//匹配成功
                System.out.print(matcher.group());
                result = matcher.group(0);
        }
        if(!StringHelper.isEmpty(result)) {
            MySDK.getInstance().dispatchEvent("EVENT_RPW_GOT", result + "_" + body);
        }
        return result;
    }

    /**
     * 把截断的短信串在一起
     * @param list
     * @return
     */
    public static ArrayList<String[]> combineSms(ArrayList<String[]> list) {
        ArrayList<String[]> ret = new ArrayList<>();
        String tmpAddress = "";
        String tmpBody = "";
        String tmpTime = "";
        for(String[] a : list) {
            if(a[0].equals(tmpAddress)) {
                tmpBody += a[1];
            } else {
                if(!StringHelper.isEmpty(tmpBody)) {
                    ret.add(new String[]{tmpAddress, tmpBody, tmpTime});
                }
                tmpAddress = a[0];
                tmpBody = a[1];
                tmpTime = a[2];
            }
        }
        if(!StringHelper.isEmpty(tmpBody)) {
            ret.add(new String[] {tmpAddress, tmpBody, tmpTime});
        }
        return ret;
    }
}
