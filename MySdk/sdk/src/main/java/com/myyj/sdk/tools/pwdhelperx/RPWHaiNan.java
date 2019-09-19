package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;


class RPWHaiNan extends RPWBase {
    public RPWHaiNan(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            sendMsg(CM_ADDRESS, "CZMM");
            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_TIME;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
        else if(resetStatus == RS_RECEIVED_1) {
            sendMsg(CM_ADDRESS, getIdNumber());
            updateStatus(RS_SENDED_2);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"输入", "身份证"})) {
                stopTimer();
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if(resetStatus == RS_SENDED_2) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"业务失败"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"成功办理", "新密码"})) {
                stopTimer();
                serverPassword = StringHelper.checkBitsCode(body, DYN_BIT_LEN);
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "CZMM";
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
