package com.myyj.sdk.tools.pwdhelperx;

import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.LogHelper;

import static com.myyj.sdk.msdk.isRpwSkip;


class RPWHeBei extends RPWBase {
    public RPWHeBei(String phoneNumber, String idNumber, ResultCallback callback) {
        super(phoneNumber, idNumber, callback);
    }

    private final static int DYN_BIT_LEN = 6;
    private String tmpAddress = "";

    public void updateStatusLogic() {
        if(resetStatus == RS_START) {
            if(!isRpwSkip())
            {
                sendMsg(CM_ADDRESS, "MMCZ");
            }else{
                sendMsg(CM_ADDRESS, "MMCZ", new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            tmpAddress = "100862220101";
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
        else if(resetStatus == RS_RECEIVED_1) {
            if(!isRpwSkip())
            {
                sendMsg(tmpAddress, this.getIdNumber());
            }else{
                sendMsg(tmpAddress, this.getIdNumber(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            tmpAddress = "100862220102";
                            changeStateIfTimeout(RS_SENDED_2, RS_RECEIVED_2);
                        }
                    }
                });
            }
            updateStatus(RS_SENDED_2);
            finishStatus = FS_FAIL_ID;
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_RECEIVED_2) {
            if(!isRpwSkip())
            {
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword());
            }else{
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            tmpAddress = "100862220103";
                            changeStateIfTimeout(RS_SENDED_3, RS_RECEIVED_3);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_3);
            finishStatus = FS_FAIL_ID;
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_RECEIVED_3) {
            if(!isRpwSkip())
            {
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword());
            }else{
                sendMsg(tmpAddress, MySDK.getInstance().getDefaultServicePassword(), new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if(state == 0) {
                            serverPassword = MySDK.getInstance().getDefaultServicePassword();
                            changeStateIfTimeout(RS_SENDED_4, RS_FINISH);
                        }
                    }
                });
            }

            updateStatus(RS_SENDED_4);
            finishStatus = FS_FAIL_ID;
            startTimer(TIMEOUT_VALUE, RS_FINISH);
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);
        }
    }

    public void updateReceiveLogic(String address, String body) {
        if(resetStatus == RS_SENDED_1) {
            if (!address.startsWith(CM_ADDRESS)) { //10086开始的一个号码
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"身份证", "河北移动"})) {
                stopTimer();
                tmpAddress = address; //需要回复该地址
                this.updateStatus(RS_RECEIVED_1);
            }
        } else if(resetStatus == RS_SENDED_2) {
            if (!address.startsWith(CM_ADDRESS)) { //10086开始的一个号码
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"身份证", "错误"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"请回复", "新密码"})) {
                stopTimer();
                tmpAddress = address; //需要回复该地址
                this.updateStatus(RS_RECEIVED_2);
            }
        } else if(resetStatus == RS_SENDED_3) {
            if (!address.startsWith(CM_ADDRESS)) { //10086开始的一个号码
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"身份证", "错误"})) {
                stopTimer();
                finishStatus = FS_FAIL_ID;
                this.updateStatus(RS_FINISH);
            } else if (checkStrContainsKeyWords(body, new String[]{"再次", "新密码"})) {
                stopTimer();
                tmpAddress = address; //需要回复该地址
                this.updateStatus(RS_RECEIVED_3);
            }
        } else if(resetStatus == RS_SENDED_4) {
            if (!address.startsWith(CM_ADDRESS)) { //10086开始的一个号码
                return;
            } else if (checkStrContainsKeyWords(body, new String[]{"修改成功"})) {
                stopTimer();
                tmpAddress = address; //需要回复该地址
                finishStatus = FS_SUC;
                serverPassword = MySDK.getInstance().getDefaultServicePassword();
                this.updateStatus(RS_FINISH);
            }
        }
    }

    @Override
    protected String getFirstSMS() {
        return "MMCZ";
    }

    @Override
    protected boolean needIdCardNumberAtFirst() {
        return false;
    }
}