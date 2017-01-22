package com.sunteng.x5webenginedemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sunteng.x5webenginedemo.Tool.X5WebView;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * X5WebEngineDemo Created by baishixian on 2017/1/20.
 */
public class GameActivity extends AppCompatActivity {
    private static final String APP_NAME_UA = "Android_X5_CORE_WEB";
    private FrameLayout mViewParent;
    private WebView x5WebView;
    private android.webkit.WebView mSystemWebView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Set layout
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.game_layout);
        mViewParent = (FrameLayout)findViewById(R.id.game_parent);

        Intent intent = getIntent();
        if (intent != null){
            String webViewType = intent.getStringExtra(Constant.WEBVIEW_ENGINE_TYPE);
            if (webViewType.equals(Constant.X5_WEBVIEW_TAG)){
                initX5WebView();
            }else {
                initSystemWebView();
            }
        }

        showProgressBar();
        if (x5WebView != null){
            Toast.makeText(GameActivity.this, "正在使用X5内核运行H5游戏" , Toast.LENGTH_SHORT).show();
            x5WebView.loadUrl("http://developer.egret.com/cn/article/index/id/963");
        }else{
            Toast.makeText(GameActivity.this, "正在使用System内核运行H5游戏" , Toast.LENGTH_SHORT).show();
            mSystemWebView.loadUrl("http://developer.egret.com/cn/article/index/id/963");
        }

       /*Tbs视频播放器,页面H5视频播放
        public static boolean canUseTbsPlayer(Context context) //判断当前Tbs播放器是否已经可以使用。
        public static void openVideo(Context context, String videoUrl) //直接调用播放接口，传入视频流的url
        public static void openVideo(Context context, String videoUrl, Bundle extraData) //extraData对象是根据定制需要传入约定的信息，没有需要可以传如null
        */

    }

    private void initSystemWebView() {
        mSystemWebView = new android.webkit.WebView(GameActivity.this);
        mSystemWebView.setTag(Constant.SYSTEM_WEBVIEW_TAG);
        mViewParent.addView(mSystemWebView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        mSystemWebView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                return false;
            }

            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(android.webkit.WebView view,
                                                                             android.webkit.WebResourceRequest request) {
                // TODO Auto-generated method stub
                return super.shouldInterceptRequest(view, request);
            }



            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                cancelProgressBar();
            }
        });

        initSystemWebViewSettings();
    }

    private void initX5WebView() {
        x5WebView = new X5WebView(GameActivity.this);
        x5WebView.setTag(Constant.X5_WEBVIEW_TAG);
        mViewParent.addView(x5WebView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        x5WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              WebResourceRequest request) {
                // TODO Auto-generated method stub

                Log.e("should", "request.getUrl().toString() is " + request.getUrl().toString());

                return super.shouldInterceptRequest(view, request);
            }



            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                cancelProgressBar();
            }
        });
        x5WebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2, JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            IX5WebChromeClient.CustomViewCallback callback;

            ///////////////////////////////////////////////////////////
            //
            /**
             * 全屏播放配置
             */
            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
                FrameLayout normalView = (FrameLayout) findViewById(R.id.web_filechooser);
                ViewGroup viewGroup = (ViewGroup) normalView.getParent();
                viewGroup.removeView(normalView);
                viewGroup.addView(view);
                myVideoView = view;
                myNormalView = normalView;
                callback = customViewCallback;
            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView arg0,
                                             ValueCallback<Uri[]> arg1, FileChooserParams arg2) {
                // TODO Auto-generated method stub
                Log.e("app", "onShowFileChooser");
                return super.onShowFileChooser(arg0, arg1, arg2);
            }

            @Override
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String captureType) {
            }


            @Override
            public boolean onJsAlert(WebView arg0, String arg1, String arg2, JsResult arg3) {
                /**
                 * 这里写入你自定义的window alert
                 */
                Log.i("onReceivedTitle", "setX5webview = null");
                return super.onJsAlert(null, "www.baidu.com", "aa", arg3);
            }

            /**
             * 对应js 的通知弹框 ，可以用来实现js 和 android之间的通信
             */


            @Override
            public void onReceivedTitle(WebView arg0, final String arg1) {
                super.onReceivedTitle(arg0, arg1);
                Log.i("onReceivedTitle", "webpage title is " + arg1);

            }
        });

        initX5WebViewSettings();
    }


    private void showProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProgressDialog = new ProgressDialog(GameActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            mProgressDialog = new ProgressDialog(GameActivity.this);
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("游戏加载中，请稍后");
        mProgressDialog.show();
    }


    private void cancelProgressBar() {
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    private void initX5WebViewSettings() {
        WebSettings webSetting = x5WebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        //webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        //webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
    }

    private void initSystemWebViewSettings() {
        android.webkit.WebSettings webSetting = mSystemWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        //webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        //webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(android.webkit.WebSettings.PluginState.ON_DEMAND);
        //webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (x5WebView != null && x5WebView.canGoBack()){
                x5WebView.goBack();// 返回前一个页面
                return true;
            }else if (mSystemWebView != null  && mSystemWebView.canGoBack()){
                mSystemWebView.goBack();// 返回前一个页面
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new android.support.v7.app.AlertDialog.Builder(GameActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new android.support.v7.app.AlertDialog.Builder(GameActivity.this);
        }
        builder.setIcon(R.mipmap.ic_launcher).setTitle("提示").setMessage("是否退出当前游戏界面?").setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).create().show();
    }
}
