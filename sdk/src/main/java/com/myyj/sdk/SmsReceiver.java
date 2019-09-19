package com.myyj.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.PhoneInfoHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;

import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.d("SmsReceiver " + " " + intent);
        Object[] object = (Object[]) intent.getExtras().get("pdus");
        ArrayList<String[]> list = new ArrayList<>();
        for (Object pdus : object) {
            byte[] pdusMsg = (byte[]) pdus;
            SmsMessage sms = SmsMessage.createFromPdu(pdusMsg);
            String address = sms.getOriginatingAddress();//发送短信的手机号
            String body = sms.getMessageBody();//短信内容
            list.add(new String[] {address, body, "" + sms.getTimestampMillis()});
        }
        list = StringHelper.combineSms(list);
        for(String[] a : list) {
            String address = a[0];
            String body = a[1];
            long time = Long.parseLong(a[2]);
            LogHelper.d(address + " -> " + body);
            if (!PhoneInfoHelper.isVivo() && !PhoneInfoHelper.isOppo()) {
                SuperSmsManager.getInstance().onReceivedSms(address, body, time);
            }
        }
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
        for(String[] a : list) {
            if(a[0].equals(tmpAddress)) {
                tmpBody += a[1];
            } else {
                ret.add(new String[] {tmpAddress, tmpBody});
                tmpAddress = a[0];
                tmpBody = a[1];
            }
        }
        if(!StringHelper.isEmpty(tmpBody)) {
            ret.add(new String[] {tmpAddress, tmpBody});
        }
        return ret;
    }
}
