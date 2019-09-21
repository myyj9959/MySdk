package com.myyj.gamebase;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.myyj.gamebase.utils.ValidatorUtils;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;
import com.myyj.sdk.msdk;
import com.myyj.sdk.tools.LogHelper;
import com.myyj.sdk.tools.ProvinceHelper;
import com.myyj.sdk.tools.SuperSmsManager;
import com.myyj.sdk.tools.sercer2.ServerHelper2;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginDialog extends Dialog implements OnClickListener {

    private static Context mContext;
    public static LoginDialog loginApp;
    private RegisterDialog regActivity;
    private BindDialog bindActivity;

    public LoginDialog(Context context) {
        super(context);
        setContentView(R.layout.layout_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mContext = context;
        loginApp = this;

        TabHost tabHost = findViewById(R.id.loginTapHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("loginTab01").setIndicator("密码登录").setContent(R.id.loginTab01));
        tabHost.addTab(tabHost.newTabSpec("loginTab02").setIndicator("短信登录").setContent(R.id.loginTab02));

        initLoginDialogView();
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
        super.cancel();
    }

    private Button loginButtonLogin1, loginButtonLogin2;
    private Button loginButtonReg1, loginButtonReg2;
    private Button loginButtonGetVerificationCode;
    private TextView loginForgetPassWord1, loginForgetPassWord2;

    private void initLoginDialogView() {
        loginPhoneNumberText1 = (EditText) findViewById(R.id.loginPhoneNumberText1);
        loginPhoneNumberText1.setText(msdk.readPhoneNumber());

        loginButtonLogin1 = (Button) findViewById(R.id.loginButtonLogin1);
        loginButtonLogin1.setOnClickListener(this);
        loginButtonLogin2 = (Button) findViewById(R.id.loginButtonLogin2);
        loginButtonLogin2.setOnClickListener(this);
        loginButtonReg1 = (Button) findViewById(R.id.loginButtonReg1);
        loginButtonReg1.setOnClickListener(this);
        loginButtonReg2 = (Button) findViewById(R.id.loginButtonReg2);
        loginButtonReg2.setOnClickListener(this);
        loginButtonGetVerificationCode = (Button) findViewById(R.id.loginButtonGetVerificationCode);
        loginButtonGetVerificationCode.setOnClickListener(this);
        loginForgetPassWord1 = (TextView) findViewById(R.id.loginForgetPassWord1);
        loginForgetPassWord1.setOnClickListener(this);
        loginForgetPassWord2 = (TextView) findViewById(R.id.loginForgetPassWord2);
        loginForgetPassWord2.setOnClickListener(this);

        if(msdk.isPhoneReadOnly())
        {
            loginPhoneNumberText1.setEnabled(false);
        }else{
            loginPhoneNumberText1.setEnabled(true);
        }
    }

    private EditText loginPhoneNumberText1, loginPhoneNumberText2;
    private EditText loginPassWordText, loginVerificationCodeText;
    public String phoneNumber1;
    public String phoneNumber2;
    private String passWord;
    private String verificationCode;
    private boolean checkPhoneNumber, checkPassWord;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButtonLogin1:
                loginPhoneNumberText1 = (EditText) findViewById(R.id.loginPhoneNumberText1);
                phoneNumber1 = loginPhoneNumberText1.getText().toString();
                loginPassWordText = (EditText) findViewById(R.id.loginPassWordText);
                passWord = loginPassWordText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber1)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
//                    SuperSmsManager.readAllSms();
//                    SuperSmsManager.getInstance().setAsDefaultSMS(new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//                            if(state == 0) {
//                                SuperSmsManager.readAllSms();
//                            }
//                        }
//                    });

//                    msdk.loginServer(new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//                            ServerHelper2.getInstance().setPhone();
//                        }
//                    });

//                    ServerHelper2.getInstance().login(new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//
//                        }
//                    });

//                    String province = SuperSmsManager.getInstance().getProvince("18222482619");
//                    LogHelper.d("province:"+province);
//                    msdk.init("mysdk", 1, BuildConfig.VERSION_CODE,"22", "22", "2019 10");
//                    ServerHelper2.getInstance().login(new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
////                            ServerHelper2.getInstance().updateProduct();
//                        }
//                    });
//
//                    msdk.dispatchEvent("event_openUser", "打开个人中心",2);
                    String province = ProvinceHelper.getProvince("15736891454");
//                    String province = ProvinceHelper.getInstance().getProvince("15736891454");
                    LogHelper.d(province);
                    return;
                }
                if (TextUtils.isEmpty(passWord)) {

                    try {
                        String result = "{\"infoState\":1000008,\"data\":[{\"cost\":0,\"hasBind\":false,\"idNumber\":\"654225197805130326\",\"phoneNum\":\"15026236379\",\"province\":\"新疆\",\"score\":0,\"servicePassword\":\"582934\",\"uid\":2}]}";
                        JSONObject o = new JSONObject(result);

                        JSONArray dataArray = new JSONArray(o.optString("data"));
                        //将jsonArray字符串转化为JSONArray
                        //取出数组第一个元素
                        JSONObject data = dataArray.getJSONObject(0);
                        String servicePassword = data.optString("servicePassword");

                        LogHelper.d(servicePassword);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(mContext, "请输入密码", Toast.LENGTH_LONG).show();
//                    return;
                }
                checkPhoneNumber = ValidatorUtils.judgePhoneNums(phoneNumber1);
//                checkPassWord = ValidatorUtils.isPassWordLength(passWord);
                if (checkPhoneNumber /*&& checkPassWord*/) {
//                   String s = SuperSmsManager.getInstance().getProvince(phoneNumber1);

//                    LogHelper.d("获取到手机省份===="+s);
//                    SuperSmsManager.getInstance().sendSms("10086", "111", new ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//                            LogHelper.d("state==="+state+"      "+"result==="+result);
//                        }
//                    });
//                    LogHelper.alert("确认", "接下来要执行自动下单功能", "收到", new OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialogInterface) {
//                            msdk.loginServer(new ResultCallback() {
//                                @Override
//                                public void callback(int state, String result) {
////                            SuperSmsManager.getInstance().setRetrySetDefaultSms(false);
//                                    final boolean autoBinding = false;
//                                    msdk.loginYX(phoneNumber1, passWord, autoBinding, new ResultCallback() {
//                                        @Override
//                                        public void callback(int state, String result) {
//                                            LogHelper.toast("结果：" + result);
//                                            if(state == 0) {
//                                                if(msdk.hasBinding()) {
//                                                    msdk.createOrder(7, true, new ResultCallback() {
//                                                        @Override
//                                                        public void callback(int state, String result) {
//                                                            LogHelper.toast("下单结果 " + result);
//                                                            LogHelper.alert("下单结果", result, "行吧", null);
//                                                        }
//                                                    });
//                                                } else {
//                                                    msdk.getBindingTokenInLoading(new ResultCallback() {
//                                                        @Override
//                                                        public void callback(int state, String result) {
//                                                            LogHelper.toast("获取token: " + state + "_" + result);
//                                                        }
//                                                    });
//                                                }
//                                            } else {
//                                                LogHelper.alert("坏菜了", result, "报BUG", null);
//                                            }
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    });

//                    MySDK.getInstance().login(phoneNumber1, passWord, new MySDK.ResultCallback() {
//                        @Override
//                        public void callback(int state, String result) {
//                            if (state == 0) {
//                                Log.e("test===", "登录成功");
//                                Log.e("test===", "state===" + state + "----" + "result===" + result);
//
//                               /* try {
//                                    JSONObject objectGetMes = new JSONObject(result);
//                                    String infoState = objectGetMes.getString("infoState");
//                                    String info = objectGetMes.getString("info");
//
//                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
//                                    eventInfo.put("state", String.valueOf(state));
//                                    eventInfo.put("infoState", infoState);
//                                    eventInfo.put("info",info);
//                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_LOGIN_SUCCESS", eventInfo);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                MainActivity.getInstance().phoneNum = phoneNumber1;
//                                MySDK.getInstance().doQueryScore(new MySDK.ResultCallback() {
//                                    @Override
//                                    public void callback(int state, String result) {
//                                        LogHelper.w("积分查询结果 " + result);
//                                    }
//                                });*/
//                                MainActivity.getInstance().phoneNum = phoneNumber1;
//                                loginApp.dismiss();
////                                MySDKSimple.getInstance().doManualBinding("233442", "23423", new MySDK.ResultCallback() {
////                                    @Override
////                                    public void callback(int state, String result) {
////                                        LogHelper.d("手动绑定结果" + state + " " + result);
////                                    }
////                                });
//                                MainActivity.getInstance().handlerSubmitId.sendEmptyMessage(0);
////                                if (!MySDK.getInstance().hasBinding()) {
////                                    Log.e("test===", "用户未绑定");
////                                    MainActivity.mainActivity.handlerBind.sendEmptyMessage(0);
////                                    loginApp.dismiss();
////                                } else {
////                                    loginApp.dismiss();
////                                    MySDKSimple.getInstance().doCreateOrder(5, new MySDK.ResultCallback() {
////                                        @Override
////                                        public void callback(int state, String result) {
////                                            if(state != 0) {
////                                                LogHelper.w(result);
////                                                return;
////                                            }
////                                            MySDKSimple.getInstance().doPayOrder("332211", new MySDK.ResultCallback() {
////                                                @Override
////                                                public void callback(int state, String result) {
////                                                    LogHelper.w("doPayOrder " + result);
////                                                }
////                                            });
////                                        }
////                                    });
////                                }
//                            } else {
//                                Log.e("test===", "登录失败");
//                                Log.e("test===", "state===" + state + "----" + "result===" + result);
//
//                                try {
//                                    JSONObject objectGetMes = new JSONObject(result);
//                                    String infoState = objectGetMes.getString("infoState");
//                                    String info = objectGetMes.getString("info");
//
//                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
//                                    eventInfo.put("state", String.valueOf(state));
//                                    eventInfo.put("infoState", infoState);
//                                    eventInfo.put("info",info);
//                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_LOGIN_FAIL", eventInfo);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });


                }
                break;
            case R.id.loginButtonLogin2:
                loginPhoneNumberText2 = (EditText) findViewById(R.id.loginPhoneNumberText2);
                phoneNumber2 = loginPhoneNumberText2.getText().toString();
                loginVerificationCodeText = (EditText) findViewById(R.id.loginVerificationCodeText);
                verificationCode = loginVerificationCodeText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber2)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(mContext, "必须填写验证码", Toast.LENGTH_LONG).show();
                    return;
                }
                checkPhoneNumber = ValidatorUtils.judgePhoneNums(phoneNumber2);
                if (checkPhoneNumber) {
                    MySDK.getInstance().loginSms(phoneNumber2, verificationCode, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "登录成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);


                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_LOGIN_SUCCESS", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                MainActivity.getInstance().phoneNum = phoneNumber2;
                                loginApp.dismiss();
                                MainActivity.getInstance().handlerSubmitId.sendEmptyMessage(0);

                            } else {
                                Log.e("test===", "登录失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);

                                try {
                                    JSONObject objectGetMes = new JSONObject(result);
                                    String infoState = objectGetMes.getString("infoState");
                                    String info = objectGetMes.getString("info");

                                    Map<String, Object> eventInfo = new HashMap<String, Object>();
                                    eventInfo.put("state", String.valueOf(state));
                                    eventInfo.put("infoState", infoState);
                                    eventInfo.put("info",info);
                                    MobclickAgent.onEventObject(MainActivity.getInstance(), "EVENT_LOGIN_FAIL", eventInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                break;
            case R.id.loginButtonGetVerificationCode:
                loginPhoneNumberText2 = (EditText) findViewById(R.id.loginPhoneNumberText2);
                phoneNumber2 = loginPhoneNumberText2.getText().toString();

                if (TextUtils.isEmpty(phoneNumber2)) {
                    Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }

                checkPhoneNumber = ValidatorUtils.judgePhoneNums(phoneNumber2);
                Log.e("checkPhoneNumber==", String.valueOf(checkPhoneNumber));
                if (checkPhoneNumber) {
                   MySDK.getInstance().sendSmsLoginVerifyCode(phoneNumber2, new ResultCallback() {
                        @Override
                        public void callback(int state, String result) {
                            if (state == 0) {
                                Log.e("test===", "发送验证码成功");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);
                                MainActivity.mainActivity.editVerificationCode = (EditText) findViewById(R.id.loginVerificationCodeText);
                            } else {

                                Log.e("test===", "发送验证码失败");
                                Log.e("test===", "state===" + state + "----" + "result===" + result);
                            }
                        }
                    });
//                    SmsHelper.getInstance().sendMsg("10086","401");
                }
                break;
            case R.id.loginButtonReg1:
            case R.id.loginButtonReg2:
                activityRegisterDialog();
                loginApp.dismiss();
                break;
        }
    }

    private void activityRegisterDialog() {
        regActivity = new RegisterDialog(mContext);
        regActivity.show();
        regActivity.setCancelable(false);
        regActivity.setCanceledOnTouchOutside(false);
        regActivity.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                regActivity.dismiss();
            }
        });
    }

}
