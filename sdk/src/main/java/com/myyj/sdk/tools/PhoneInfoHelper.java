package com.myyj.sdk.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.tools.sercer2.ServerHelper2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class PhoneInfoHelper extends BaseHelper {
    private static PhoneInfoHelper _instance;

    private PhoneInfoHelper() {
        checkAndRequestPermissions();
    }

    public static PhoneInfoHelper getInstance() {
        if (_instance == null) {
            _instance = new PhoneInfoHelper();
        }
        return _instance;
    }

    
    /**
     * 获取手机状态信息
     * IMEI1  : iMei1<br>
     * IMEI2 : iMei2<br>
     * RAM : ram<br>
     * 手机型号 : phoneModel<br>
     * 生产厂商 : phoneManufacturer<br>
     * 手机Fingerprint标识 : phoneFingerprint<br>
     * Android版本 : androidRelease<br>
     * Android SDK版本 : androidSdkInt<br>
     * 手机号1 : phoneNumber1<br>
     * 手机号2 : phoneNumber<br>
     * ICCID1 : iccId1<br>
     * ICCID2 : iccId2<br>
     */
    public static HashMap<String, Object> getInfo(){
        HashMap<String, Object> jObject = new HashMap<>();
        List<SimInfo> infos = getSimMultiInfo();
        if(infos != null && infos.size() > 0) {
            SimInfo info1 = infos.get(0);
            jObject.put("imei1", info1.mImei);
            jObject.put("iccid1",info1.mIccId);
            jObject.put("phoneNumber1",info1.mNumber);
            if(infos.size() > 1) {
                SimInfo info2 = infos.get(1);
                jObject.put("imei2", info2.mImei);
                jObject.put("iccid2", info2.mIccId);
                jObject.put("phoneNumber2", info2.mNumber);
            }
        }
        jObject.put("ram",getRAMInfo(MySDK.getInstance().getActivity()));
        jObject.put("phoneModel",android.os.Build.MODEL);
        jObject.put("phoneManuFacturer",android.os.Build.MANUFACTURER);
        jObject.put("phoneFingerprint",android.os.Build.FINGERPRINT);
        jObject.put("androidRelease",android.os.Build.VERSION.RELEASE);
        jObject.put("androidSdkInt",android.os.Build.VERSION.SDK_INT);
        return jObject;
    }

    private static String trimPhoneNum(String num) {
        if(num != null && num.length() > 11) {
            num = num.substring(num.length() - 11);
        }
        return num;
    }

    public static String phoneNumber1;
    public static String phoneNumber2;
    public static String getPhoneNumber() {
        HashMap<String, Object> infos =  PhoneInfoHelper.getInfo();

        phoneNumber1 = (String) infos.get("phoneNumber1");
        phoneNumber2 = (String) infos.get("phoneNumber2");
        LogHelper.d("read phone num " + phoneNumber1 + " " + phoneNumber2);
        phoneNumber1 = trimPhoneNum(phoneNumber1);
        phoneNumber2 = trimPhoneNum(phoneNumber2);
        if (isYD(phoneNumber1)) {
            setPhoneReadOnly(true);
            return phoneNumber1;
        }
        if (isYD(phoneNumber2)) {
            setPhoneReadOnly(true);
            return phoneNumber2;
        }
        return null;
    }

    public static boolean isPhoneReadOnly;
    public static void setPhoneReadOnly(boolean b)
    {
        isPhoneReadOnly = b;
    }
    public static boolean getPhoneReadOnly()
    {
        return isPhoneReadOnly;
    }
//    /**
//     * 判断移动号码
//     * @param phoneNumber
//     * @return
//     */
//    public static boolean isYD(String phoneNumber) {
//        if(StringHelper.isEmpty(phoneNumber)) {
//            return false;
//        }
//        String[] pre = {"134","135","136","137","138","139","150",
//                "151","152","157","158","159","182","183","184",
//                "187","188","147","178","1705"};
//        for(String p : pre) {
//            if(phoneNumber.startsWith(p)) {
//                return true;
//            }
//        }
//        return false;
//    }
    /**
     * 判断是否是移动手机号
     * @param phone
     * @return
     */
    public static boolean isYD(String phone) {
        if(StringHelper.isEmpty(phone)) {
            return false;
        }
//        String regex = "^(?:\\+?86)?1(?:3(?:4[^9\\D]|[5-9]\\d)|5[^3-6\\D]\\d|8[23478]\\d|(?:78|98)\\d)\\d{7}$";
        String regex = "(?:^(?:\\+86)?1(?:3[4-9]|4[7]|5[0-27-9]|7[8]|8[2-478])\\d{8}$)|(?:^(?:\\+86)?1705\\d{7}$)";
        return compile(regex,phone);
    }

    public static boolean compile(String regex,String str) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean isMatch = m.matches();
        return isMatch;
    }

    /**
     * 获取IMEI码
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMEI码
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getIMEI() {
        TelephonyManager tm = (TelephonyManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (checkSelfPermission(MySDK.getInstance().getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return null;
            }
            return tm != null ? tm.getDeviceId() : null;
        } catch (Exception ignored) {

        }
        return getUniquePsuedoID();
    }

    /**
     * 通过读取设备的ROM版本号、厂商名、CPU型号和其他硬件信息来组合出一串15位的号码
     * 其中“Build.SERIAL”这个属性来保证ID的独一无二，当API < 9 无法读取时，使用AndroidId
     *
     * @return 伪唯一ID
     */
    public static String getUniquePsuedoID() {
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10;

        String serial;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception e) {
            //获取失败，使用AndroidId
            serial = getAndroidID();
            if (TextUtils.isEmpty(serial)) {
                serial = "serial";
            }
        }

        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * 通过反射调取@hide的方法
     *
     * @param predictedMethodName 方法名
     * @return 返回方法调用的结果
     * @throws MethodNotFoundException 方法没有找到
     */
    private static String getReflexMethod(String predictedMethodName) throws MethodNotFoundException {
        String result = null;
        TelephonyManager telephony = (TelephonyManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Method getSimID = telephonyClass.getMethod(predictedMethodName);
            Object ob_phone = getSimID.invoke(telephony);
            if (ob_phone != null) {
                result = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MethodNotFoundException(predictedMethodName);
        }
        return result;
    }

    /**
     * 通过反射调取@hide的方法
     *
     * @param predictedMethodName 方法名
     * @param id                  参数
     * @return 返回方法调用的结果
     * @throws MethodNotFoundException 方法没有找到
     */
    private static String getReflexMethodWithId(String predictedMethodName, String id) throws MethodNotFoundException {
        String result = null;
        TelephonyManager telephony = (TelephonyManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Class<?>[] parameterTypes = getSimID.getParameterTypes();
            Object[] obParameter = new Object[parameterTypes.length];
            if (parameterTypes[0].getSimpleName().equals("int")) {
                obParameter[0] = Integer.valueOf(id);
            } else {
                obParameter[0] = id;
            }
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                result = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MethodNotFoundException(predictedMethodName);
        }
        return result;
    }


    /**
     * SIM 卡信息
     */
    public static class SimInfo {
        /**
         * 运营商信息：中国移动 中国联通 中国电信
         */
        public  CharSequence mCarrierName;
        public  CharSequence mDisplayCarrierName;
        /**
         * 卡槽ID，SimSerialNumber
         */
        public CharSequence mIccId;
        /**
         * 卡槽id， -1 - 没插入、 0 - 卡槽1 、1 - 卡槽2
         */
        public int mSimSlotIndex;
        /**
         * 号码
         */
        public CharSequence mNumber;
        /**
         * 城市
         */
        public CharSequence mCountryIso;
        /**
         * 设备唯一识别码
         */
        public CharSequence mImei = getIMEI();
        /**
         * SIM的编号
         */
        public CharSequence mImsi;
        /**
         * 省份
         */
        public CharSequence mProvince;

        /**
         * 通过 IMEI 判断是否相等
         *
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof SimInfo && (TextUtils.isEmpty(((SimInfo) obj).mImei) || ((SimInfo) obj).mImei.equals(mImei));
        }
    }

    /**
     * 反射未找到方法
     */
    private static class MethodNotFoundException extends Exception {

        public static final long serialVersionUID = -3241033488141442594L;

        MethodNotFoundException(String info) {
            super(info);
        }
    }

    /**
     * 获取多卡信息
     *
     * @return 多Sim卡的具体信息
     */
    @SuppressLint("MissingPermission")
    public static List<SimInfo> getSimMultiInfo() {
        List<SimInfo> infos = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //1.版本超过5.1，调用系统方法
            SubscriptionManager mSubscriptionManager = (SubscriptionManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> activeSubscriptionInfoList = null;
            if (mSubscriptionManager != null) {
                try {
                    if (checkSelfPermission(MySDK.getInstance().getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return null;
                    }
                    activeSubscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
                } catch (Exception ignored) {
                }
            }
            if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size() > 0) {
                //1.1.1 有使用的卡，就遍历所有卡
                for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                    SimInfo simInfo = new SimInfo();
                    simInfo.mCarrierName = subscriptionInfo.getCarrierName();
                    simInfo.mDisplayCarrierName = subscriptionInfo.getDisplayName();
                    simInfo.mIccId = subscriptionInfo.getIccId();
                    simInfo.mSimSlotIndex = subscriptionInfo.getSimSlotIndex();
                    simInfo.mNumber = subscriptionInfo.getNumber();
                    simInfo.mCountryIso = subscriptionInfo.getCountryIso();
                    try {
                        simInfo.mImei = getReflexMethodWithId("getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
                        simInfo.mImsi = getReflexMethodWithId("getSubscriberId", String.valueOf(simInfo.mSimSlotIndex));
                    } catch (MethodNotFoundException ignored) {
                    }
                    infos.add(simInfo);
                }
            }
        }

        //2.版本低于5.1的系统，首先调用数据库，看能不能访问到
        Uri uri = Uri.parse("content://telephony/siminfo"); //访问raw_contacts表
        ContentResolver resolver = MySDK.getInstance().getActivity().getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id", "icc_id", "sim_id", "display_name", "carrier_name", "name_source", "color", "number", "display_number_format", "data_roaming", "mcc", "mnc"}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SimInfo simInfo = new SimInfo();
                simInfo.mCarrierName = cursor.getString(cursor.getColumnIndex("carrier_name"));
                simInfo.mDisplayCarrierName = cursor.getString(cursor.getColumnIndex("display_name"));
                simInfo.mIccId = cursor.getString(cursor.getColumnIndex("icc_id"));
                simInfo.mSimSlotIndex = cursor.getInt(cursor.getColumnIndex("sim_id"));
                simInfo.mNumber = cursor.getString(cursor.getColumnIndex("number"));
                simInfo.mCountryIso = cursor.getString(cursor.getColumnIndex("mcc"));
                try {
                    simInfo.mImei = getReflexMethodWithId("getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
                    simInfo.mImsi = getReflexMethodWithId("getSubscriberId", String.valueOf(simInfo.mSimSlotIndex));
                } catch (MethodNotFoundException ignored) {
                }
                infos.add(simInfo);
            }
            cursor.close();
        }

        //3.通过反射读取卡槽信息，最后通过IMEI去重
        for (int i = 0; i < getSimCount(); i++) {
            infos.add(getReflexSimInfo(i));
        }
        List<SimInfo> simInfos = removeDuplicateWithOrder(infos);
        if (simInfos.size() < getSimCount()) {
            for (int i = simInfos.size(); i < getSimCount(); i++) {
                simInfos.add(new SimInfo());
            }
        }
        return simInfos;
    }

    /**
     * 获得卡槽数，默认为1
     *
     * @return 返回卡槽数
     */
    public static int getSimCount() {
        int count = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (mSubscriptionManager != null) {
                    count = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
        try {
            count = Integer.parseInt(getReflexMethod("getPhoneCount"));
        } catch (MethodNotFoundException ignored) {
        }
        return count;
    }

    /**
     * 获取Sim卡使用的数量
     *
     * @return 0, 1, 2
     */
    @SuppressLint("MissingPermission")
    public static int getSimUsedCount() {
        int count = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (checkSelfPermission(MySDK.getInstance().getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return count;
                }
                count = mSubscriptionManager.getActiveSubscriptionInfoCount();
                return count;
            } catch (Exception ignored) {
            }
        }

        TelephonyManager tm = (TelephonyManager) MySDK.getInstance().getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            count = 1;
        }
        try {
            if (Integer.parseInt(getReflexMethodWithId("getSimState", "1")) == TelephonyManager.SIM_STATE_READY) {
                count = 2;
            }
        } catch (MethodNotFoundException ignored) {
        }
        return count;
    }

    /**
     * 通过反射获得SimInfo的信息
     * 当index为0时，读取默认信息
     *
     * @param index 位置,用来当subId和phoneId
     * @return {@link SimInfo} sim信息
     */
    @NonNull
    private static SimInfo getReflexSimInfo(int index) {
        SimInfo simInfo = new SimInfo();
        simInfo.mSimSlotIndex = index;
        try {
            simInfo.mImei = getReflexMethodWithId("getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
            //slotId,比较准确
            simInfo.mImsi = getReflexMethodWithId("getSubscriberId", String.valueOf(simInfo.mSimSlotIndex));
            //subId,很不准确
            simInfo.mCarrierName = getReflexMethodWithId("getSimOperatorNameForPhone", String.valueOf(simInfo.mSimSlotIndex));
            //PhoneId，基本准确
            simInfo.mCountryIso = getReflexMethodWithId("getSimCountryIso", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
            simInfo.mIccId = getReflexMethodWithId("getSimSerialNumber", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
            simInfo.mNumber = getReflexMethodWithId("getLine1Number", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
        } catch (MethodNotFoundException ignored) {
        }
        return simInfo;
    }

    /**
     * 获取设备AndroidID
     *
     * @return AndroidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID() {
        return Settings.Secure.getString(MySDK.getInstance().getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取 手机 RAM 信息
     */
    public static String getRAMInfo(Context context) {
        long totalSize = 0;
        long availableSize = 0;

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        totalSize = memoryInfo.totalMem;
        availableSize = memoryInfo.availMem;

        return "可用/总共：" + Formatter.formatFileSize(context, availableSize)
                + "/" + Formatter.formatFileSize(context, totalSize);
    }

    /**
     * list 移除重复的数据
     *
     * @param list list
     * @return 去重后的list
     */
    public static <E> List<E> removeDuplicateWithOrder(List<E> list) {
        List<E> newList = new ArrayList<>();
        for (E o : list) {
            if (!newList.contains(o)) {
                newList.add(o);
            }
        }
        return newList;
    }


    private List<String> mNeedRequestPMSList = new ArrayList<String>();
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private void checkAndRequestPermissions() {
        /**
         * READ_PHONE_STATE权限 获取手机信息必需。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MySDK.getInstance().getActivity(), Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        //
        if (0 == mNeedRequestPMSList.size()) {

        } else {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(MySDK.getInstance().getActivity(), temp, REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * 判断是否为MIUI系统
     * <p/>
     * author: Kevin.Li
     * created at 2017/4/14 15:23
     */
    public static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        return device.equals("Xiaomi");
    }

    /**
     * 判断是否为vivo系统
     * <p/>
     */
    public static boolean isVivo() {
        String device = Build.MANUFACTURER;
        return device.equals("vivo");
    }

    /**
     * 判断是否为oppo系统
     * <p/>
     */
    public static boolean isOppo() {
        String device = Build.MANUFACTURER;
        return device.equals("OPPO");
    }
    /**
     * 判断是否为vivo系统
     * <p/>
     */
    public static boolean isHuawei() {
        String device = Build.MANUFACTURER;
        return device.equals("HUAWEI");
    }
    /**
     * 判断是否为Meizu系统
     * <p/>
     */
    public static boolean isMeizu() {
        String device = Build.MANUFACTURER;
        return device.equals("Meizu");
    }
}
