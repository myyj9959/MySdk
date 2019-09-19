package com.myyj.gamebase.utils;

import android.text.TextUtils;
import android.widget.Toast;

import com.myyj.gamebase.MainActivity;
import com.myyj.sdk.MySDK;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtils {

    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    public static boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11) && isMobileNO(phoneNums)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断一个字符串的位数
     *
     * @param str
     * @param length
     * @return
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            Toast.makeText(MySDK.getInstance().getActivity(), "手机号码为空", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (str.length() == length) {
                return true;
            } else {
                Toast.makeText(MySDK.getInstance().getActivity(), "手机号码不合法", Toast.LENGTH_SHORT).show();
                return false;
            }
            //return str.length() == length ? true : false;
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobileNums) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums)) {
            Toast.makeText(MySDK.getInstance().getActivity(), "手机号码为空！", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (mobileNums.matches(telRegex)) {
                return true;
            } else {
                Toast.makeText(MySDK.getInstance().getActivity(), "手机号码不合法", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }


    public static boolean judgeCodeNums(String codeNums) {
        if (isMatchLength1(codeNums, 4)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMatchLength1(String str, int length) {
        if (str.isEmpty()) {
            Toast.makeText(MainActivity.getInstance(), "验证码为空", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (str.length() == length) {
                return true;
            } else {
                Toast.makeText(MainActivity.getInstance(), "验证码错误", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    /**
     * 判断一个字符串的位数
     *
     * @param str
     * @return
     */
    public static boolean isPassWordLength(String str) {
        if (str.isEmpty()) {
            Toast.makeText(MainActivity.getInstance(), "密码为空", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Pattern Password_Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(.{8,20})$");
            Matcher matcher = Password_Pattern.matcher(str);
            if (matcher.matches()) {
                return true;
            } else {
                Toast.makeText(MainActivity.getInstance(), "密码必须由8-16位数字字母组成", Toast.LENGTH_SHORT).show();
                return false;
            }

        }


    }

    /*判断Email合法性*/
    public static boolean isEmail(String email) {
        if (email == null)
            return false;
        String rule = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(rule);
        matcher = pattern.matcher(email);
        if (matcher.matches())
            return true;
        else {
            Toast.makeText(MainActivity.getInstance(), "邮箱地址不合法", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    /**
     * 比较真实完整的判断身份证号码的工具
     *
     * @param IdCard 用户输入的身份证号码
     * @return [true符合规范, false不符合规范]
     */
    public static boolean isRealIDCard(String IdCard) {
        if (IdCard != null) {
            int correct = new IdCardUtil(IdCard).isCorrect();
            String errMsg = new IdCardUtil(IdCard).getErrMsg1(correct);
            if (0 == correct) {// 符合规范
                return true;
            }
            else{
                Toast.makeText(MainActivity.getInstance(), errMsg, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
