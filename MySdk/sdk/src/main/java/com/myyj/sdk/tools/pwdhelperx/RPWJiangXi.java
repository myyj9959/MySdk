package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;


class RPWJiangXi extends RPWBase {
    public RPWJiangXi(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            sendMsg(CM_ADDRESS, "");
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
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"成功修改", "新密码"})) {
                stopTimer();
                serverPassword = StringHelper.checkBitsCode(body,DYN_BIT_LEN);
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

}
