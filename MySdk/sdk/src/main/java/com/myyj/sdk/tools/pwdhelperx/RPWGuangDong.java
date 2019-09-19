package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.LogHelper;

import static com.myyj.sdk.msdk.isRpwSkip;


class RPWGuangDong extends RPWBase {
    public RPWGuangDong(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;

    public void updateStatusLogic() {
        if (resetStatus == RS_START) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "801");
            }
            else{
                sendMsg(CM_ADDRESS, "801", new ResultCallback() {
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
        } else if (resetStatus == RS_FINISH) {
            finish(finishStatus);
        } else if (resetStatus == RS_RECEIVED_1) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword());
            }
            else{
                sendMsg(CM_ADDRESS, getIdNumber() + "#" + MySDK.getInstance().getDefaultServicePassword() + "#" + MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = MySDK.getInstance().getDefaultServicePassword();
                            changeStateIfTimeout(RS_SENDED_2, RS_RECEIVED_2);
                        }
                    }
                });
            }
            updateStatus(RS_SENDED_2);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        } else if (resetStatus == RS_RECEIVED_2) {
            if(!isRpwSkip()){
                    sendMsg(CM_ADDRESS, "CZMM");
            }
            else{
                sendMsg(CM_ADDRESS, "CZMM", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            changeStateIfTimeout(RS_SENDED_3, RS_RECEIVED_3);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_3);
            finishStatus = FS_FAIL_TIME;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        } else if (resetStatus == RS_RECEIVED_3) {
            if(!isRpwSkip()){
                sendMsg(CM_ADDRESS, getIdNumber() + "#" + "58293417" + "#" + "58293417");
            }
            else{
                sendMsg(CM_ADDRESS, getIdNumber() + "#" + "58293417" + "#" + "58293417", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = "58293417";
                            changeStateIfTimeout(RS_SENDED_4, RS_FINISH);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_4);
            finishStatus = FS_FAIL_ID;
            serverPassword = "";
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if (resetStatus == RS_SENDED_1) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"重置密码", "开户"})) {
                stopTimer();
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if (resetStatus == RS_SENDED_2) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"输入有误", "重新输入"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"短信", "设置", "成功"})) {
                stopTimer();
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"办理失败", "过于简单"})) {
                stopTimer();
                this.updateStatus(RS_RECEIVED_2);
            }

        } else if (resetStatus == RS_SENDED_3) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"重置密码", "开户"})) {
                stopTimer();
                this.updateStatus(RS_RECEIVED_3);
            }
        } else if (resetStatus == RS_SENDED_4) {
            if (!address.equals(CM_ADDRESS)) {
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"输入有误", "重新输入"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"短信", "设置", "成功"})) {
                stopTimer();
                serverPassword = "58293417";
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "801";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return false;
    }
}
