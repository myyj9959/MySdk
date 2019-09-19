package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;


class RPWGanSu extends RPWBase {
    public RPWGanSu(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if (resetStatus == RS_START) {
            sendMsg(CM_ADDRESS, "CZMM"); //CZMM
            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_TIME;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        } else if (resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if (resetStatus == RS_SENDED_1) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"新密码", "成功"})) {
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
    protected boolean needIdCardNumberAtAll() {
        return false;
    }

    @Override
    protected String defaultResult() {
        return "";
    }
}
