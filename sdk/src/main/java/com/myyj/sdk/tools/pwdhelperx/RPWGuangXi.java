package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.StringHelper;

import static com.myyj.sdk.msdk.isRpwSkip;

class RPWGuangXi extends RPWBase {
    public RPWGuangXi(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;
    private String tmpAddress = "";
    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            serverPassword = "";
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "6012#" + getIdNumber());
            }else{
                sendMsg(CM_ADDRESS, "6012#" + getIdNumber(), new ResultCallback() {
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
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
        else if(resetStatus == RS_RECEIVED_1) {
            if(!isRpwSkip())
            {
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword());
            }else{
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
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
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"验证通过", "新密码"})) {
                stopTimer();
                tmpAddress = address;
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if(resetStatus == RS_SENDED_2) {
            if (!address.startsWith(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"对不起","不一致"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"密码", "重置成功"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "6012#"+getIdNumber();
    }

}
