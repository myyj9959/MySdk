package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;

import static com.myyj.sdk.msdk.isRpwSkip;

/**
 * 陕西
 */
class RPWShanXii extends RPWBase {
    public RPWShanXii(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if (resetStatus == RS_START) {
            serverPassword = "";
            if(!isRpwSkip()){
                sendMsg(CM_ADDRESS, "CZMM#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword());
            }
            else{
                sendMsg(CM_ADDRESS, "CZMM#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = MySDK.getInstance().getDefaultServicePassword();
                            changeStateIfTimeout(RS_SENDED_1, RS_FINISH);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_1);
            finishStatus = FS_FAIL_ID;
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        } else if (resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if (resetStatus == RS_SENDED_1) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"证件号码", "不正确"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"重置", "注意保管"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "CZMM#" + getIdNumber()+"#"+ MySDK.getInstance().getDefaultServicePassword();
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return super.needIdCardNumberAtFirst();
    }
}
