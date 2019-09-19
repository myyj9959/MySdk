package com.myyj.gamebase;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.myyj.gamebase.utils.ValidatorUtils;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterDialog extends Dialog implements OnClickListener {

    private static Context mContext;
    private RegisterDialog regApp;
    private static TabHost tabHost;
    private static int regTabId = 0;

    public RegisterDialog(Context context) {
        super(context);

        setContentView(R.layout.layout_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mContext = context;
        regApp = this;

        tabHost = findViewById(R.id.regTapHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("regTab01").setIndicator("验证手机号").setContent(R.id.regTab01));
        tabHost.addTab(tabHost.newTabSpec("regTab02").setIndicator("填写账号信息").setContent(R.id.regTab02));
        tabHost.addTab(tabHost.newTabSpec("regTab03").setIndicator("注册成功").setContent(R.id.regTab03));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                tabHost.setCurrentTab(regTabId);
            }
        });
        initRegisterDialogView();
    }

    Handler handlerTab = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    regTabId = 1;
                    regApp.tabHost.setCurrentTab(regTabId);
                    break;
                case 2:
                    regTabId = 2;
                    regApp.tabHost.setCurrentTab(regTabId);
                    break;
            }
        }
    };

    private Button regButtonClose, regButtonGetVerificationCode, regButtonVerificationPhone, regButtonRegister, regButtonRegisterSuccess;

    private void initRegisterDialogView() {
        regButtonClose = (Button) findViewById(R.id.regButtonClose);
        regButtonClose.setOnClickListener(this);
        regButtonGetVerificationCode = (Button) findViewById(R.id.regButtonGetVerificationCode);
        regButtonGetVerificationCode.setOnClickListener(this);
        regButtonVerificationPhone = (Button) findViewById(R.id.regButtonVerificationPhone);
        regButtonVerificationPhone.setOnClickListener(this);
        regButtonRegister = (Button) findViewById(R.id.regButtonRegister);
        regButtonRegister.setOnClickListener(this);
        regButtonRegisterSuccess = (Button) findViewById(R.id.regButtonRegisterSuccess);
        regButtonRegisterSuccess.setOnClickListener(this);
    }

    private EditText regPhoneNumberText1, regPhoneNumberText2, regVerificationCodeText, regEmailText, regPassWordText, regRePassWordText;
    private String phoneNumber, passWord, verificationCode, emailText, passwordText, confirmPasswordText;
    private boolean checkPhoneNumber, checkPassWord, checkRePassWord, checkEmail;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.regButtonGetVerificationCode:
                regPhoneNumberText1 = (EditText) findViewById(R.id.regPhoneNumberText1);
                phoneNumber = regPhoneNumberText1.getText().toString();
                /*if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }*/
                checkPhoneNumber = ValidatorUtils.judgePhoneNums(phoneNumber);
                Log.e("checkPhoneNumber==", String.valueOf(checkPhoneNumber));

                if (checkPhoneNumber) {
                    MySDK.getInstance().sendSmsLoginVerifyCode(phoneNumber, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "发送验证码成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);
                                MainActivity.mainActivity.editVerificationCode = (EditText) findViewById(R.id.regVerificationCodeText);
                            } else {
                                Log.e("test===", "发送验证码失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);
                            }
                        }
                    });
                }
                break;
            case R.id.regButtonVerificationPhone:
                regPhoneNumberText1 = (EditText) findViewById(R.id.regPhoneNumberText1);
                phoneNumber = regPhoneNumberText1.getText().toString();
                regVerificationCodeText = (EditText) findViewById(R.id.regVerificationCodeText);
                verificationCode = regVerificationCodeText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(mContext, "必须填写验证码", Toast.LENGTH_LONG).show();
                    return;
                }

                checkPhoneNumber = ValidatorUtils.judgePhoneNums(phoneNumber);
                if (checkPhoneNumber) {
                    MySDK.getInstance().registerSmsVerify(phoneNumber, verificationCode, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "验证手机号成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                handlerTab.sendEmptyMessage(1);

                                regPhoneNumberText2 = (EditText) findViewById(R.id.regPhoneNumberText2);
                                regPhoneNumberText2.setText(phoneNumber);
                            } else {
                                Log.e("test===", "验证手机号失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);
                            }
                        }
                    });
                }
                break;
            case R.id.regButtonRegister:
                regEmailText = (EditText) findViewById(R.id.regEmailText);
                emailText = regEmailText.getText().toString();
                regPassWordText = (EditText) findViewById(R.id.regPassWordText);
                passwordText = regPassWordText.getText().toString();
                regRePassWordText = (EditText) findViewById(R.id.regRePassWordText);
                confirmPasswordText = regRePassWordText.getText().toString();

                checkEmail = ValidatorUtils.isEmail(emailText);
                checkPassWord = ValidatorUtils.isPassWordLength(passwordText);
                checkRePassWord = ValidatorUtils.isPassWordLength(confirmPasswordText);

                if (!passwordText.equals(confirmPasswordText)) {
                    Toast.makeText(mContext, "两次密码不一样", Toast.LENGTH_LONG).show();
                    return;
                }
                if (checkEmail && checkPassWord && checkRePassWord) {
                    MySDK.getInstance().setUserInfo(phoneNumber, verificationCode, emailText, passwordText, confirmPasswordText, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "注册成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_REGISTER_SUCCESS", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                handlerTab.sendEmptyMessage(2);
//                                regTabId = 2;
//                                tabHost.setCurrentTab(regTabId);
                            } else {
                                Log.e("test===", "注册失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_REGISTER_FAIL", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                break;
            case R.id.regButtonClose:
            case R.id.regButtonRegisterSuccess:
                regTabId = 0;
                regApp.dismiss();
                MainActivity.handlerLogin.sendEmptyMessage(0);
                break;
        }
    }
}
