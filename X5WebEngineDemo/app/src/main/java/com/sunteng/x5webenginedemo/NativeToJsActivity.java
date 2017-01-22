package com.sunteng.x5webenginedemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sunteng.x5webenginedemo.Tool.WebViewJavaScriptFunction;
import com.sunteng.x5webenginedemo.Tool.X5WebView;

public class NativeToJsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nativeToJsEdit;
    private TextView nativeToJsNumTextView;


    private Handler handler;

    private static final int MSG=0;
    private static final int NUM=1;
    private static final int MSG_SUBMIT=2;
    private static final int CLOSE_SUBMIT=3;

    private int num=0;
    private String msg="X5 WebView";

    private X5WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_js_activity);

        this.handler=new Handler(Looper.myLooper()){

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch(msg.what){
                    case NUM:
                        NativeToJsActivity.this.nativeToJsNumTextView.setText(String.valueOf(num));
                        break;
                    case MSG:
                        NativeToJsActivity.this.nativeToJsEdit.setText(NativeToJsActivity.this.msg);
                        break;
                    case MSG_SUBMIT:
                        NativeToJsActivity.this.msg = NativeToJsActivity.this.nativeToJsEdit.getEditableText().toString();
                        break;
                    case CLOSE_SUBMIT:
                        Toast.makeText(NativeToJsActivity.this.getApplicationContext() , "Js 异步触发 Android 窗口关闭事件", Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);

            }

        };
        mWebView = (X5WebView) findViewById(R.id.x5web_native2js);
        mWebView.loadUrl("file:///android_asset/webpage/jsToJava.html");

        mWebView.addJavascriptInterface(new WebViewJavaScriptFunction() {

            @Override
            public void onJsFunctionCalled(String tag) {
                // TODO Auto-generated method stub
                Log.i("jsToAndroid","onJsFunctionCalled tag : " + tag);
            }
            ///////////////////////////////////////////////
            //javascript to java methods
            @JavascriptInterface
            public void onSubmit(String s){
                Log.i("jsToAndroid","onSubmit happend! " + s);
                NativeToJsActivity.this.msg = s;
                Message.obtain(handler, MSG).sendToTarget();
            }

            @JavascriptInterface
            public void onSubmitNum(String s){
                Log.i("jsToAndroid","onSubmitNum happend! " + s);
                NativeToJsActivity.this.num = Integer.parseInt(s);
                Message.obtain(handler, NUM).sendToTarget();
            }


            /**
             * java 调用 js方法 并且 传值
             * 步骤：1、调用 js函数  2、js回调一个android方法得到参数  3、js处理函数
             * @return
             */
            @JavascriptInterface
            public String getAndroidMsg(){
                Log.i("jsToAndroid","onSubmitNum happend!");
                return NativeToJsActivity.this.msg;
            }

            @JavascriptInterface
            public String getAndroidNum(){
                Log.i("jsToAndroid","onSubmitNum happend!");
                return String.valueOf(NativeToJsActivity.this.num);
            }



            /**
             * 各种类型的传递
             */
            @JavascriptInterface
            public void getManyValue(String key,String value){

                Log.i("jsToAndroid", "get key is:"+key+"  value is:"+value);
            }

            /**
             * 关闭当前的窗口
             */
            @JavascriptInterface
            public void closeCurrentWindow(){
                Message.obtain(handler, CLOSE_SUBMIT).sendToTarget();
                NativeToJsActivity.this.finish();
            }
        }, "Android");

        initView();
    }

    private void initView() {

        nativeToJsEdit = (EditText)findViewById(R.id.edit_native_js_edit);
        this.nativeToJsEdit.setHint("输入与js交互的内容");

        nativeToJsNumTextView = (TextView)findViewById(R.id.text_native_js_num);
        this.nativeToJsNumTextView.setText("" + this.num);

        findViewById(R.id.bt_native_js_edit).setOnClickListener(this);
        findViewById(R.id.bt_native_js_num_add).setOnClickListener(this);
        findViewById(R.id.bt_native_js_num_minuse).setOnClickListener(this);
        findViewById(R.id.web_native_js_close_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int num = Integer.parseInt(NativeToJsActivity.this.nativeToJsNumTextView.getText().toString());

        switch (v.getId()){
            case R.id.bt_native_js_edit:
                Message.obtain(handler, MSG_SUBMIT).sendToTarget();
                NativeToJsActivity.this.mWebView.loadUrl("javascript:returnMsg()");
                break;
            case R.id.web_native_js_close_btn:
                NativeToJsActivity.this.mWebView.loadUrl("javascript:closeWnd()");
                break;
            case R.id.bt_native_js_num_add:
                NativeToJsActivity.this.num++;
                NativeToJsActivity.this.num=(++num);
                Message.obtain(handler, NUM).sendToTarget();
                NativeToJsActivity.this.mWebView.loadUrl("javascript:returnNum()");
                break;
            case R.id.bt_native_js_num_minuse:
                NativeToJsActivity.this.num=(--num);
                NativeToJsActivity.this.mWebView.loadUrl("javascript:returnNum()");
                break;
        }
    }
}
