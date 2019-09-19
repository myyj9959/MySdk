package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;


class RPWJiangSu extends RPWBase {
    public RPWJiangSu(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            sendMsg(CM_ADDRESS, "CZMM#"+getIdNumber());
            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);

        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"重置密码", "身份证", "不正确"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);

            } else if (checkStrContainsKeyWords(body, new String[]{"密码", "XGSJMM"})) {
                stopTimer();
                serverPassword = StringHelper.checkBitsCode(body, DYN_BIT_LEN);
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "CZMM#"+getIdNumber();
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return super.needIdCardNumberAtFirst();
    }

    @Override
    protected String defaultResult() {
        return "";
    }
}
