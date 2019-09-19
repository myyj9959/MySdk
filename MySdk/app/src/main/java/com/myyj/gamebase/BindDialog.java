package com.myyj.gamebase;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.myyj.gamebase.utils.ValidatorUtils;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BindDialog extends Dialog implements View.OnClickListener {

    private static Context mContext;
    public static BindDialog bindApp;

    public BindDialog(Context context) {
        super(context);
        setContentView(R.layout.layout_bind);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mContext = context;
        bindApp = this;

        initBindDialogView();

//        if (!LoginDialog.loginApp.phoneNumber1.equals("")) {
//            bindPhoneNumberDesc = (EditText) findViewById(R.id.bindPhoneNumberDesc);
//            bindPhoneNumberDesc.setText(LoginDialog.loginApp.phoneNumber1);
//        } else if (!LoginDialog.loginApp.phoneNumber2.equals("")) {
//            bindPhoneNumberDesc = (EditText) findViewById(R.id.bindPhoneNumberDesc);
//            bindPhoneNumberDesc.setText(LoginDialog.loginApp.phoneNumber2);
//        }
            bindPhoneNumberText = (EditText) findViewById(R.id.bindPhoneNumberText);
            String number = MySDK.getInstance().getPhoneNumber();
        bindPhoneNumberText.setText(number);
    }

    private CheckBox bindCheckAgreement;
    private Button bindButtonSubmit, bindButtonGetVerificationCode;
    public EditText bindPhoneNumberText, bindServicePasswordText, bindVerificationCodeText;

    private void initBindDialogView() {
        bindCheckAgreement = (CheckBox) findViewById(R.id.bindCheckAgreement);
        bindButtonGetVerificationCode = (Button) findViewById(R.id.bindButtonGetVerificationCode);
        bindButtonGetVerificationCode.setOnClickListener(this);
        bindButtonSubmit = (Button) findViewById(R.id.bindButtonSubmit);
        bindButtonSubmit.setOnClickListener(this);
        bindButtonSubmit.setEnabled(false);

        bindCheckAgreement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    bindButtonSubmit.setEnabled(true);
                } else {
                    bindButtonSubmit.setEnabled(false);
                }
            }
        });
    }

    private String bindPhoneNum, bindServicePassword, bindVerificationCode;
    boolean checkPhoneNumber;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bindButtonGetVerificationCode:
                bindPhoneNumberText = (EditText) findViewById(R.id.bindPhoneNumberText);
                bindPhoneNum = bindPhoneNumberText.getText().toString();
                MySDK.getInstance().sendBindingVerifyCode(bindPhoneNum, new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        if (state == 0) {
                            Log.e("test===", "发送验证码成功");
                            Log.e("test===", "state===" + state + "----" + "result===" + result);
                            MainActivity.mainActivity.editVerificationCode = (EditText) findViewById(R.id.bindVerificationCodeText);
                        } else {
                            Log.e("test===", "发送验证码失败");
                            Log.e("test===", "state===" + state + "----" + "result===" + result);
                        }
                    }
                });
                break;
            case R.id.bindButtonSubmit:
                bindPhoneNumberText = (EditText) findViewById(R.id.bindPhoneNumberText);
                bindPhoneNum = bindPhoneNumberText.getText().toString();
                bindServicePasswordText = (EditText) findViewById(R.id.bindServicePasswordText);
                bindServicePassword = bindServicePasswordText.getText().toString();
                bindVerificationCodeText = (EditText) findViewById(R.id.bindVerificationCodeText);
                bindVerificationCode = bindVerificationCodeText.getText().toString();

                if (TextUtils.isEmpty(bindPhoneNum)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(bindServicePassword)) {
                    Toast.makeText(mContext, "必须填写客服服务密码", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(bindVerificationCode)) {
                    Toast.makeText(mContext, "必须填写短信验证码", Toast.LENGTH_LONG).show();
                    return;
                }
                checkPhoneNumber = ValidatorUtils.judgePhoneNums(bindPhoneNum);
                this.hide();
                if (checkPhoneNumber) {
                    Log.e("test===", "bindPhoneNum===" + bindPhoneNum + "----" + "bindServicePassword===" + bindServicePassword + "----" + "bindVerificationCode===" + bindVerificationCode);
                    MySDK.getInstance().bindChangyoyoPlatform(bindPhoneNum, bindServicePassword, bindVerificationCode, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "绑定成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_BIND_SUCCESS", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("test===", "绑定失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_BIND_FAIL", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                break;
        }
    }
}
