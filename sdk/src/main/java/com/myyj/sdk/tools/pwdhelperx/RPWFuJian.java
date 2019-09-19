package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;

import static com.myyj.sdk.msdk.isRpwSkip;


class RPWFuJian extends RPWBase {
    public RPWFuJian(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            serverPassword = "";
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "40*" + MySDK.getInstance().getDefaultServicePassword()+ "*" +getIdNumber() + "#");
            }else{
                sendMsg(CM_ADDRESS, "40*" + MySDK.getInstance().getDefaultServicePassword()+ "*" +getIdNumber() + "#", new ResultCallback() {
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
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"成功重置", "服务密码"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
            else if (checkStrContainsKeyWords(body, new String[]{"使用过", "请更改"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
            else if(checkStrContainsKeyWords(body,new String[]{"对不起","失败"}))
            {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "40*" + MySDK.getInstance().getDefaultServicePassword()+ "*" +getIdNumber() + "#";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return super.needIdCardNumberAtFirst();
    }
}
