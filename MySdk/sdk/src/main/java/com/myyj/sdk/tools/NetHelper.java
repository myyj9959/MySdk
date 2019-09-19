package com.myyj.sdk.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.baidu.aip.ocr.AipOcr;
import com.myyj.sdk.MySDK;
import com.myyj.sdk.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetHelper extends BaseHelper {
    private static final byte[] LOCKER = new byte[0];
    private static NetHelper mInstance;
    private OkHttpClient mOkHttpClient;

    private NetHelper() {
        okhttp3.OkHttpClient.Builder ClientBuilder = new okhttp3.OkHttpClient.Builder();
        ClientBuilder.readTimeout(20, TimeUnit.SECONDS);//读取超时
        ClientBuilder.connectTimeout(6, TimeUnit.SECONDS);//连接超时
        ClientBuilder.writeTimeout(60, TimeUnit.SECONDS);//写入超时
        //支持HTTPS请求，跳过证书验证
        ClientBuilder.sslSocketFactory(createSSLSocketFactory());
        ClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mOkHttpClient = ClientBuilder.build();
    }

    /**
     * 单例模式获取NetUtils
     *
     * @return
     */
    public static NetHelper getInstance() {
        if (mInstance == null) {
            synchronized (LOCKER) {
                if (mInstance == null) {
                    mInstance = new NetHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * get请求，同步方式，获取网络数据，是在主线程中执行的，需要新起线程，将其放到子线程中执行
     *
     * @param url
     * @return
     */
    public Response getDataSynFromNet(String url) {
        LogHelper.d("getDataSynFromNet " + url);
        //1 构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();
        //2 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        //3 执行Call，得到response
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            LogHelper.e(e);
        }
        return response;
    }

//    /**
//     * post请求，同步方式，提交数据，是在主线程中执行的，需要新起线程，将其放到子线程中执行
//     *
//     * @param url
//     * @param bodyParams
//     * @return
//     */
//    public Response postDataSynToNet(String url, Map<String, String> bodyParams) {
//        //1构造RequestBody
//        RequestBody body = setRequestBody(bodyParams);
//        //2 构造Request
//        Request.Builder requestBuilder = new Request.Builder();
//        Request request = requestBuilder.post(body).url(url).build();
//        //3 将Request封装为Call
//        Call call = mOkHttpClient.newCall(request);
//        //4 执行Call，得到response
//        Response response = null;
//        try {
//            response = call.execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return response;
//    }

    /**
     * 自定义网络回调接口
     */
    public interface MyNetCall {
        void success(Call call, Response response) throws IOException;

        void failed(Call call, IOException e);
    }

//    /**
//     * get请求，异步方式，获取网络数据，是在子线程中执行的，需要切换到主线程才能更新UI
//     *
//     * @param url
//     * @param myNetCall
//     * @return
//     */
//    public void getDataAsynFromNet(String url, final MyNetCall myNetCall) {
//        //1 构造Request
//        Request.Builder builder = new Request.Builder();
//        Request request = builder.get().url(url).build();
//        //2 将Request封装为Call
//        Call call = mOkHttpClient.newCall(request);
//        //3 执行Call
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                myNetCall.failed(call, e);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                myNetCall.success(call, response);
//
//            }
//        });
//    }

    /**
     * 循环等待GET结果
     */
    private String _stringFromNet;
    public String getStringFromNet(final String url) {
        _stringFromNet = null;
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Response response = getDataSynFromNet(url);
                _stringFromNet = "";
                try {
                    _stringFromNet = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0);
        try {
            int tick = 0;
            while(_stringFromNet == null && tick++ < 30) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _stringFromNet;
    }

    /**
     * post请求，异步方式，提交数据，是在子线程中执行的，需要切换到主线程才能更新UI
     *
     * @param url
     * @param bodyParams
     * @param myNetCall
     */
    public void postDataAsynToNet(String url, Map<String, String> bodyParams, final MyNetCall myNetCall) {
        assert url != null && url.length() > 0;
        //1构造RequestBody
        RequestBody body = setRequestBody(bodyParams);
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        //3 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        LogHelper.i("postDataAsynToNet " + url + DataHelper.fromMap(bodyParams));
        //4 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call, response);
            }
        });
    }

    public void postJsonAsynToNet(String url, JSONObject json, final MyNetCall myNetCall) {
        assert url != null && url.length() > 0;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, String.valueOf(json));
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        //3 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        LogHelper.i("postJsonAsynToNet " + url + json.toString());
        //4 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call, response);
            }
        });
    }

    /**
     * post的请求参数，构造RequestBody
     *
     * @param BodyParams
     * @return
     */
    private RequestBody setRequestBody(Map<String, String> BodyParams) {
        RequestBody body = null;
        okhttp3.FormBody.Builder formEncodingBuilder = new okhttp3.FormBody.Builder();
        if (BodyParams == null) {
            BodyParams = new HashMap<>();
        }
        BodyParams.put("source", MySDK.getInstance().getChannelSource());
        Iterator<String> iterator = BodyParams.keySet().iterator();
        String key = "";
        while (iterator.hasNext()) {
            key = iterator.next();
            String value = BodyParams.get(key);
            Log.d("post http", "post_Params===" + key + "====" + value);
            formEncodingBuilder.add(key, BodyParams.get(key));
        }
        body = formEncodingBuilder.build();
        return body;

    }

//    /**
//     * 判断网络是否可用
//     *
//     * @param context
//     * @return
//     */
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm == null) {
//        } else {
//            //如果仅仅是用来判断网络连接
//            //则可以使用cm.getActiveNetworkInfo().isAvailable();
//            NetworkInfo[] info = cm.getAllNetworkInfo();
//            if (info != null) {
//                for (int i = 0; i < info.length; i++) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public void getRealUrl(final String url, final ResultCallback callback) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getFinalUrl(url, callback);
            }
        });
    }

    private WebView _webView;
    private String _userAgent;
    public WebView initWebView() {
        LogHelper.d("initWebView");
        if(_webView == null) {
            _webView = new WebView(getActivity());
            _webView.setBackgroundColor(Color.GRAY);
//            _webView.setAlpha(0f);
            _webView.setVisibility(WebView.INVISIBLE);
            _userAgent = _webView.getSettings().getUserAgentString();
            _webView.getSettings().setUseWideViewPort(true);
            _webView.getSettings().setLoadWithOverviewMode(true);
            FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(800, 1600);
            getActivity().addContentView(_webView, params3);
        }
        return _webView;
    }

    public void closeWebView() {
        if(_webView == null) {
            return;
        }
        LogHelper.d("closeWebView");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(null);
                ((ViewGroup)_webView.getParent()).removeView(_webView);
                _webView.destroy();
                _webView = null;
                _bindingResultCallback = null;
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void getFinalUrl(final String initialUrl, final ResultCallback finalUrl) {
        LogHelper.d("getFinalUrl " + initialUrl.length());
        closeWebView();
        final WebView webView = initWebView();
//        webView.setAlpha(0f);
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogHelper.d("onPageFinished " + url);
                if (!url.equals(initialUrl)) {
                    closeWebView();
                    finalUrl.callback(0, url);
                }
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // handler.cancel();// Android默认的处理方式，WebView变成空白页
                handler.proceed();  // 接受所有网站的证书
                // handleMessage(Message msg); // 进行其他处理
                LogHelper.w("webview SSL error " + error.toString());
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.d("webview", "onLoadResource " + url);
//                https://open.jf.10086.cn/bind/goDestination.service?retVal=888&msg=undefined&tokenId=e19e20d1cbd5b578ab02c3fe7a118281&partnerId=2100&interCode=JF0001-1
//                https://m.changyoyo.com/apigate/cmcc/binding/result.htm
//                http://39.106.1.23:8080/sms/callbackUrl?response={%22requestId%22:%222019080700403363466619%22,%22signType%22:%22MD5%22,%22partnerId%22:%22S1000358%22,%22outTokenId%22:%2213934198175%22,%22type%22:%22mobile%22,%22version%22:%221.0.0%22,%22message%22:%22%E6%88%90%E5%8A%9F%22,%22interCode%22:%22CYS0001%22,%22hmac%22:%22bcf76bd1e3530c7b1b8605b0eba02846%22,%22resultCode%22:%220000%22,%22points%22:%220%22,%22reserved2%22:%222%22,%22reserved1%22:%2227%22}
//                if(url.contains("/sms/callbackUrl")) {
//                    _bindingResultCallback.callback(0, "绑定成功");
//                    closeWebView();
//                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                LogHelper.w("onReceivedError " + error.toString());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                LogHelper.w("onReceivedError " + errorResponse.toString());
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                LogHelper.w("onReceivedHttpAuthRequest " + host + " realm: " + realm);
            }
        });
        webView.loadUrl(initialUrl);
    }

//    /**
//     * 模拟点击网页绑定
//     * @param phone
//     * @param pwd
//     * @param sms
//     */
    ResultCallback _bindingResultCallback;
//    public void doWebBinding(String phone, String pwd, String sms, MySDK.ResultCallback callback) {
//        LogHelper.d("doWebBinding " + phone + " " + pwd + " " + sms);
//        _bindingResultCallback = callback;
//        simulateTouchArray(initWebView(), getBindTouchPoints(phone, pwd, sms), new MySDK.ResultCallback() {
//            @Override
//            public void callback(int state, String result) {
//                checkBindingResult();
//            }
//        });
//    }
//
//    /**
//     * 检查绑定结果
//     */
//    private void checkBindingResult() {
//        LogHelper.d("checkBindingResult");
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                MySDK.getInstance().doQueryScore(new MySDK.ResultCallback() {
//                    @Override
//                    public void callback(int state, String result) {
//                        LogHelper.d("绑定结果 " + state + " " + result);
//                        closeWebView();
//                        _bindingResultCallback.callback(state, result);
//                    }
//                });
//            }
//        }, 5000);
//    }
//
//    public ArrayList<int[]> getBindTouchPoints(String phone, String pwd, String sms) {
//        final HashMap<String, int[]> map = new HashMap<>();
//        map.put("1", new int[]{100, 1200, 0});
//        map.put("2", new int[]{400, 1200, 0});
//        map.put("3", new int[]{700, 1200, 0});
//        map.put("4", new int[]{100, 1300, 0});
//        map.put("5", new int[]{400, 1300, 0});
//        map.put("6", new int[]{700, 1300, 0});
//        map.put("7", new int[]{100, 1400, 0});
//        map.put("8", new int[]{400, 1400, 0});
//        map.put("9", new int[]{700, 1400, 0});
//        map.put("0", new int[]{400, 1550, 0});
//        map.put("phone", new int[]{400, 430, 50});
//        map.put("pwd", new int[]{400, 550, 50});
//        map.put("sms", new int[]{400, 680, 50});
//        map.put("commit", new int[]{400, 1000, 100});
//
//        ArrayList<int[]> ret = new ArrayList<>();
//        ret.add(map.get("phone"));
//        for(char c : phone.toCharArray()) {
//            ret.add(map.get(String.valueOf(c)));
//        }
//        ret.add(map.get("pwd"));
//        for(char c : pwd.toCharArray()) {
//            ret.add(map.get(String.valueOf(c)));
//        }
//        ret.add(map.get("sms"));
//        for(char c : sms.toCharArray()) {
//            ret.add(map.get(String.valueOf(c)));
//        }
//        ret.add(map.get("commit"));
//        return ret;
//    }
//
//    public void simulateTouch(View view, float x, float y) {
////        LogHelper.d("simulateTouch " + x + "," + y);
//        long downTime = SystemClock.uptimeMillis();
//        long eventTime = downTime + 100;
//        int metaState = 0;
//        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, metaState);
//        view.dispatchTouchEvent(motionEvent);
//
//        MotionEvent upEvent = MotionEvent.obtain(downTime + 1000, eventTime + 1000, MotionEvent.ACTION_UP, x, y, metaState);
//        view.dispatchTouchEvent(upEvent);
//    }
//
//    public void simulateTouchArray(final View view, final ArrayList<int[]> touchPoints, final MySDK.ResultCallback callbackAtFinish) {
//        LogHelper.d("simulateTouchArray ---- " + touchPoints.size());
//        final Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            int tick = 0;
//            int index = 0;
//            @Override
//            public void run() {
////                LogHelper.d("tick " + tick);
//                if(tick-- <= 0) {
//                    int[] p = touchPoints.get(index);
//                    LogHelper.d("click at " + + index + " " + p[0] + "," + p[1] + " sleep " + p[2]);
//                    simulateTouch(view, p[0], p[1]);
//                    tick = p[2];
//                    index++;
//                    if(index >= touchPoints.size()) {
//                        timer.cancel();
//                        callbackAtFinish.callback(0, "点击完成了");
//                    }
//                }
//            }
//        }, 1000, 10);
//    }
//
//    public String concatParams(Map<String,String> params) {
//        if(params.size() ==0){
//            return null;
//        }
//        StringBuilder builder = new StringBuilder();
//        Set<String> keys = params.keySet();
//        Iterator<String> iterator = keys.iterator();
//        while (iterator.hasNext()){
//            String key = iterator.next();
//            String value = null;
//            try {
//                value = URLEncoder.encode(params.get(key), "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            builder.append(String.format("%s=%s&",key, value));
//        }
//        builder.deleteCharAt(builder.lastIndexOf("&"));
//        return builder.toString();
//    }

    class WebViewCookieHandler implements CookieJar {
        private CookieManager mCookieManager = CookieManager.getInstance();

        public Context context;

        public WebViewCookieHandler(Context context) {
            this.context = context;
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            mCookieManager.setAcceptCookie(true);
            String urlString = url.toString();
            for (Cookie cookie : cookies) {
                mCookieManager.setCookie(urlString, cookie.toString());
                LogHelper.d("set cookie " + urlString + " " + cookie.toString());
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            } else {
                mCookieManager.flush();
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            String urlString = url.toString();
            String cookiesString = mCookieManager.getCookie(urlString);
            LogHelper.d("get cookies " + url + " " + cookiesString);
            if (cookiesString != null && !cookiesString.isEmpty()) {
                String[] cookieHeaders = cookiesString.split(";");
                List<Cookie> cookies = new ArrayList<>(cookieHeaders.length);
                for (String header : cookieHeaders) {
                    cookies.add(Cookie.parse(url, header));
                }
                return cookies;
            }
            return Collections.emptyList();
        }
    }

    //设置APP_ID/AK/SK
    private static final String APP_ID = "11389454";
    private static final String API_KEY = "Ud5mG4zDLmpqtsAftRivAdVB";
    private static final String SECRET_KEY = "1Xgsk4MtPh09E6W8oskjm7GQWMj3eQYO";

    private AipOcr client;

    public String getStringFromImage(String imageUrl) {
        // 初始化一个AipOcr
        if (client == null) {
            client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        }
        // 调用接口
        JSONObject res = client.basicGeneralUrl(imageUrl, new HashMap<String, String>());
        try {
            JSONArray wordsResult = (JSONArray) res.get("words_result");
            JSONObject wordsArray = (JSONObject) wordsResult.get(0);
            return wordsArray.getString("words");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //////////////////////////////////// 四条通信绑定处理 //////////////////////////////////////
    public void postDataAsynToNetForBinding(String url, final Map<String, String> bodyParams, final ResultCallback myNetCall) {
        assert url != null && url.length() > 0;
        //1构造RequestBody
        FormBody.Builder builder = new FormBody.Builder();
        String[] KEYS = {"interCode", "mobile", "partnerId", "pwd", "smsCode", "tokenId"};
        for(String k : KEYS) {
            builder.add(k, bodyParams.get(k));
        }
        RequestBody body = builder.build();
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("Host", "open.jf.10086.cn");
        requestBuilder.addHeader("Connection", "keep-alive");
        requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
        requestBuilder.addHeader("Origin", "https://open.jf.10086.cn");
        requestBuilder.addHeader("User-Agent", _userAgent);
        requestBuilder.addHeader("DNT","1");
        requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        requestBuilder.addHeader("Referer", "https://open.jf.10086.cn/bind/bindForm?tokenId=" + bodyParams.get("tokenId"));
        requestBuilder.addHeader("Accept-Encoding", "gzip, deflate, br");
        requestBuilder.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        Request request = requestBuilder.post(body).url(url).build();
        //3 将Request封装为Call
        OkHttpClient.Builder ClientBuilder = new OkHttpClient.Builder();
        ClientBuilder.readTimeout(20, TimeUnit.SECONDS);//读取超时
        ClientBuilder.connectTimeout(6, TimeUnit.SECONDS);//连接超时
        ClientBuilder.writeTimeout(60, TimeUnit.SECONDS);//写入超时
        //支持HTTPS请求，跳过证书验证
        ClientBuilder.sslSocketFactory(createSSLSocketFactory());
        ClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        OkHttpClient client = ClientBuilder.cookieJar(new WebViewCookieHandler(getActivity())).build();
        Call call = client.newCall(request);
        LogHelper.i("postDataAsynToNetForBinding " + url + DataHelper.fromMap(bodyParams));
        //4 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogHelper.w("onFailure " + e.getLocalizedMessage());
                myNetCall.callback(23, "网络问题");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogHelper.d("binding onResponse " + result);
//                {"msg":"绑定成功","partnerId":"2100","interCode":"JF0001-1","retVal":"888"}
                try {
                    JSONObject obj = new JSONObject(result);
                    if(obj.getString("retVal").equals("888")) {
                        goDestination(bodyParams, myNetCall);
                    } else {
                        String info = obj.getString("msg");
                        myNetCall.callback(111, info);
                    }
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                myNetCall.callback(33, result);
            }
        });
    }

    public void goDestination(final Map<String, String> bodyParams, final ResultCallback myNetCall) {
        LogHelper.w("afterBindingOK ");
//GET https://open.jf.10086.cn/bind/goDestination.service?retVal=888&msg=undefined&tokenId=07e2c2f8b94394338f02fd9ffd51fac1&partnerId=2100&interCode=JF0001-1 HTTP/1.1

        final String tokenId = bodyParams.get("tokenId");
        String url = "https://open.jf.10086.cn/bind/goDestination.service";
        url = url + "?retVal=888&msg=undefined&tokenId=" + tokenId ;
        url = url + "&partnerId=2100&interCode=JF0001-1";
        LogHelper.d("afterBinding get " + url);
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("Host", "open.jf.10086.cn");
        requestBuilder.addHeader("Connection", "keep-alive");
        requestBuilder.addHeader("Upgrade-Insecure-Requests", "1");
        requestBuilder.addHeader("User-Agent", _userAgent);
        requestBuilder.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        requestBuilder.addHeader("dnt","1");
        requestBuilder.addHeader("X-Requested-With", "mark.via");
        requestBuilder.addHeader("Referer", "https://open.jf.10086.cn/bind/bindForm?tokenId=" + bodyParams.get("tokenId"));
        requestBuilder.addHeader("Accept-Encoding", "gzip, deflate, br");
        requestBuilder.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");

        Request request = requestBuilder.get().url(url).build();
        //3 将Request封装为Call
        OkHttpClient.Builder ClientBuilder = new OkHttpClient.Builder();
        ClientBuilder.readTimeout(20, TimeUnit.SECONDS);//读取超时
        ClientBuilder.connectTimeout(6, TimeUnit.SECONDS);//连接超时
        ClientBuilder.writeTimeout(60, TimeUnit.SECONDS);//写入超时
        //支持HTTPS请求，跳过证书验证
        ClientBuilder.sslSocketFactory(createSSLSocketFactory());
        ClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        OkHttpClient client = ClientBuilder.cookieJar(new WebViewCookieHandler(getActivity())).build();
        Call call = client.newCall(request);
        LogHelper.i("goDestination " + url + DataHelper.fromMap(bodyParams));
        //4 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogHelper.w("onFailure " + e.getLocalizedMessage());
                myNetCall.callback(22, "网络问题");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogHelper.d("onResponse " + result);
                result(result, new ResultCallback() {
                    @Override
                    public void callback(int state, String result) {
                        LogHelper.d("RESULTTTT " + result);
                        myNetCall.callback(0, "绑定成功");
                    }
                });
//                myNetCall.success(call, response);
            }
        });
    }

    public static HashMap<String, String> getKVArrayFromHTMLStr(String str) {
        HashMap<String, String> arrays = new HashMap<String, String>();

        Pattern pattern = Pattern.compile("<input[^<>]*>"); //("\"<input [^(<>)]* >\"");//提取关键组
        Matcher matcher = pattern.matcher(str);//进行匹配
        while(matcher.find()){//匹配成功
            String _str = matcher.group();
            _str = _str.replaceAll("<input ", "");
            _str = _str.replaceAll("/>", "");
            _str = _str.replaceAll(">", "");
            String[] strArray = _str.split(" ");
            String key = getAttrByNameFromArray(strArray, "name");
            String value = getAttrByNameFromArray(strArray, "value");
            arrays.put(key, value);
        }

        return arrays;
    }

    public static String getAttrByNameFromArray(String[] array, String attName) {
        for(String str : array) {
            if(str.startsWith(attName))
            {
                if(str.contains("='")) {
                    return str.substring(str.indexOf("'")+1, str.lastIndexOf("'"));
                }
                else if(str.contains("=\"")) {
                    return str.substring(str.indexOf("\"")+1, str.lastIndexOf("\""));
                }
            }
        }
        return "";
    }

    /**
     * 第三步 跟移动同步绑定结果
     * @param result
     * @param resultCallback
     */
    public void result(String result, final ResultCallback resultCallback) {
//String url = "<form name='postSubmit' method='post' action='https://m.changyoyo.com/apigate/cmcc/binding/result.htm' >" +
//        "<input type='hidden' name='returnCode' value='0000'>" +
//"<input type='hidden' name='thirdAccount' value='U0032947432'>" +
//"<input type='hidden' name='requestId' value='P15648477328390326'>" +
//"<input type='hidden' name='hmac' value='b699f0d8404206db22bcd145328619b4'>" +
//"<input type='hidden' name='mobile' value='15798923836'>" +
//"<input type='hidden' name='signType' value='MD5'>" +
//"<input type='hidden' name='partnerId' value='2100'>" +
//"<input type='hidden' name='interCode' value='JF0001'>" +
//"<input type='hidden' name='message' value='ç»å®æå'>" +
//"<input type='hidden' name='type' value='mobile'>" +
//"<input type='hidden' name='version' value='1.0.0'>" +
//"</form>" +
//"<script>" +
//"                document.postSubmit.submit()" +
//"                </script>";
        HashMap<String, String> params = getKVArrayFromHTMLStr(result);
        String url = "https://m.changyoyo.com/apigate/cmcc/binding/result.htm";
//        returnCode=0000&thirdAccount=U0032947432&requestId=P15648477328390326&hmac=b699f0d8404206db22bcd145328619b4&mobile=15798923836&signType=MD5&partnerId=2100&interCode=JF0001&message=%E7%BB%91%E5%AE%9A%E6%88%90%E5%8A%9F&type=mobile&version=1.0.0
//        returnCode	0000
//        thirdAccount	U0032947432
//        requestId	P15648477328390326
//        hmac	b699f0d8404206db22bcd145328619b4
//        mobile	15798923836
//        signType	MD5
//        partnerId	2100
//        interCode	JF0001
//        message	绑定成功
//        type	mobile
//        version	1.0.0
        postDataAsynToNet(url, params, new MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                resultCallback.callback(0, "绑定成功");
                String result = response.body().string();
                callbackUrl(result);
            }

            @Override
            public void failed(Call call, IOException e) {
                LogHelper.w("onFailure " + e.getLocalizedMessage());
                resultCallback.callback(1, "联网问题");
            }
        });

    }

    /**
     * 第四步 向银夏回调结果
     */
    public void callbackUrl(String result) {
        HashMap<String, String> params = getKVArrayFromHTMLStr(result);
        String url = "http://39.107.6.163/sms/callbackUrl";
        try{
            url = URLDecoder.decode(params.get("callback"), "utf-8");
        }
        catch(UnsupportedEncodingException e){
           e.printStackTrace();
        }
        LogHelper.d("callbackUrl1 " + url);
        String paramStr = params.get("param");

        url = url + "?response=" + paramStr;
        LogHelper.d("callbackUrl2 " + url);
        this.getDataSynFromNet(url);
        closeWebView();
    }
}

