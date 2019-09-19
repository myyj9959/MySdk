package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;

class RPWTianJin extends RPWBase {
    public RPWTianJin(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;
    private String tmpAddress = "";

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            sendMsg(CM_ADDRESS, "2031");
            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_TIME;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
        else if(resetStatus == RS_RECEIVED_1) {
            sendMsg(tmpAddress, getIdNumber());
            updateStatus(RS_SENDED_2);
            finishStatus = FS_FAIL_ID;
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"个人户名", "入网"})) {
                stopTimer();
                tmpAddress = address;
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if(resetStatus == RS_SENDED_2) {

            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"办理失败"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"身份鉴权", "天津移动"})) {
                stopTimer();
                serverPassword = StringHelper.checkBitsCode(body, DYN_BIT_LEN);
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }

        }
    }

    @Override
    protected String getFirstSMS() {
        return "2031";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return false;
    }

    @Override
    protected String defaultResult() {
        return "";
    }
}
