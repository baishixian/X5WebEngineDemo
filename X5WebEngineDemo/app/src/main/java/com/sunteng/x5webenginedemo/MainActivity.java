package com.sunteng.x5webenginedemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sunteng.x5webenginedemo.Tool.X5WebView;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SearchView mSearchBar;
    private Toolbar mToolbar;
    private LinearLayout mParentView;
    private ValueCallback<Uri> uploadFile;
    private ProgressDialog mProgressDialog;

    private X5WebView x5WebView;
    private android.webkit.WebView mSystemWebView;

    private FrameLayout mWebController;
    private ToggleButton mChangeEngine;
    private boolean isEnableX5Engine = false;
    private TextView mHintShowInfoText;
    private long time = 0;
    private LinearLayout idel_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        mToolbar.setOnClickListener(this);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSearchBar = (SearchView) findViewById(R.id.search_bar);
        mSearchBar.setQueryHint("输入网址");
        mSearchBar.setOnQueryTextListener(onQueryTextListener);

        mParentView = (LinearLayout)findViewById(R.id.web_parent);

        mHintShowInfoText = (TextView)findViewById(R.id.tv_show_info);

        mWebController = (FrameLayout)findViewById(R.id.web_controller);

        mChangeEngine = (ToggleButton)findViewById(R.id.web_control_change_engine);
        mChangeEngine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeWebViewEngine(isChecked);
            }
        });

        findViewById(R.id.web_control_start_game).setOnClickListener(this);
        findViewById(R.id.web_control_js_native).setOnClickListener(this);
        findViewById(R.id.web_control_exit).setOnClickListener(this);

        findViewById(R.id.bt_show_qq_video).setOnClickListener(this);
        findViewById(R.id.bt_show_bilibili_video).setOnClickListener(this);

        idel_layout = (LinearLayout)findViewById(R.id.idel_layout);

    }

    private void changeWebViewEngine(boolean isChecked) {

        isEnableX5Engine = isChecked;

        if (isChecked){
            Toast.makeText(MainActivity.this, "切换到X5内核", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(MainActivity.this, "切换到系统内核", Toast.LENGTH_SHORT).show();
        }
    }

    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            String openUrl;

            if (query.startsWith("http://") || query.startsWith("https://") || query.startsWith("ftp://")){
                openUrl = query;
            }else{
                openUrl = "http://" + query;
            }

            mSearchBar.clearFocus();

            if (idel_layout != null && mParentView.findViewById(R.id.idel_layout) != null){
                mParentView.removeView(idel_layout);
            }

            if (isEnableX5Engine){
                openWebUrlByX5(openUrl);
            }else {
                openWebUrlBySystem(openUrl);
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private void openWebUrlBySystem(String openUrl) {

        showProgressBar();

        if (x5WebView != null && mParentView.findViewWithTag(Constant.X5_WEBVIEW_TAG) != null){
            Toast.makeText(MainActivity.this, "切换到了system浏览器内核，页面加载中", Toast.LENGTH_SHORT).show();
            mParentView.removeView(x5WebView);
        }

        if (mSystemWebView == null){
            intSystemWebView();
        }

        if (mParentView.findViewWithTag(Constant.SYSTEM_WEBVIEW_TAG) == null){
            mParentView.addView(mSystemWebView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }

        time = System.currentTimeMillis();
        mSystemWebView.loadUrl(openUrl);
        CookieSyncManager.getInstance().sync();
    }

    private void intSystemWebView() {
        mSystemWebView = new android.webkit.WebView(MainActivity.this);
        mSystemWebView.setTag(Constant.SYSTEM_WEBVIEW_TAG);
        mParentView.addView(mSystemWebView, new LinearLayout.LayoutParams(
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
                long costTime = System.currentTimeMillis() - time;
                TbsLog.d("time-cost", "openWebUrlBySystem cost time: "
                        + costTime);
                cancelProgressBar();
                Toast.makeText(MainActivity.this, "system浏览器内核加载 \n" + url + " \n耗时：" + costTime, Toast.LENGTH_SHORT).show();
            }
        });

        mSystemWebView.setDownloadListener(new android.webkit.DownloadListener() {

            @Override
            public void onDownloadStart(String arg0, String arg1, String arg2,
                                        String arg3, long arg4) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                builder.setIcon(R.mipmap.ic_launcher).setTitle("是否下载文件")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "I'll download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                MainActivity.this,
                                                "no refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {

                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                MainActivity.this,
                                                "refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
            }
        });


        android.webkit.WebSettings webSetting = mSystemWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm( android.webkit.WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        //webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        //webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState( android.webkit.WebSettings.PluginState.ON_DEMAND);
        //webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        CookieSyncManager.createInstance(this);
    }

    private void openWebUrlByX5(String url) {

        showProgressBar();

        if (mSystemWebView != null && mParentView.findViewWithTag(Constant.SYSTEM_WEBVIEW_TAG) != null){
            Toast.makeText(MainActivity.this, "切换到了X5浏览器内核，页面加载中", Toast.LENGTH_SHORT).show();
            mParentView.removeView(mSystemWebView);
        }

        if (x5WebView == null){
            initX5WebView();
        }

        if (mParentView.findViewWithTag(Constant.X5_WEBVIEW_TAG) == null){
            mParentView.addView(x5WebView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }

        time = System.currentTimeMillis();
        x5WebView.loadUrl(url);
        CookieSyncManager.getInstance().sync();
    }

    private void initX5WebView() {
        x5WebView = new X5WebView(MainActivity.this);
        x5WebView.setTag(Constant.X5_WEBVIEW_TAG);
        mParentView.addView(x5WebView, new LinearLayout.LayoutParams(
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
                long costTime = System.currentTimeMillis() - time;
                TbsLog.d("time-cost", "openWebUrlByX5 cost time: "
                        + costTime);
                cancelProgressBar();
                Toast.makeText(MainActivity.this, "X5内核加载 \n" + url + " \n耗时：" + costTime, Toast.LENGTH_SHORT).show();
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
                MainActivity.this.uploadFile = uploadFile;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "test"), 0);
            }


            @Override
            public boolean onJsAlert(WebView arg0, String arg1, String arg2, JsResult arg3) {
                /**
                 * 这里写入你自定义的window alert
                 */
                // AlertDialog.Builder builder = new Builder(getContext());
                // builder.setTitle("X5内核");
                // builder.setPositiveButton("确定", new
                // DialogInterface.OnClickListener() {
                //
                // @Override
                // public void onClick(DialogInterface dialog, int which) {
                // // TODO Auto-generated method stub
                // dialog.dismiss();
                // }
                // });
                // builder.show();
                // arg3.confirm();
                // return true;
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

        x5WebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String arg0, String arg1, String arg2,
                                        String arg3, long arg4) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }

                builder.setIcon(R.mipmap.ic_launcher).setTitle("是否下载文件")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "I'll download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                MainActivity.this,
                                                "no refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {

                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        // TODO Auto-generated method stub
                                        Toast.makeText(
                                                MainActivity.this,
                                                "refuse download...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
            }
        });


        WebSettings webSetting = x5WebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        //webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        //webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        CookieSyncManager.createInstance(this);
    }


    public void hideSoftInputKeyBoard(){
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProgressDialog = new ProgressDialog(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            mProgressDialog = new ProgressDialog(MainActivity.this);
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("页面加载中，请稍后");
        mProgressDialog.show();
    }


    private void cancelProgressBar() {
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar:
                mSearchBar.setIconified(false);
                break;
            case R.id.web_control_start_game:
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                if (isEnableX5Engine){
                    intent.putExtra(Constant.WEBVIEW_ENGINE_TYPE, Constant.X5_WEBVIEW_TAG);
                }else{
                    intent.putExtra(Constant.WEBVIEW_ENGINE_TYPE, Constant.SYSTEM_WEBVIEW_TAG);
                }
                startActivity(intent);
                break;
            case R.id.web_control_js_native:
                startActivity(new Intent(MainActivity.this, NativeToJsActivity.class));
                break;
            case R.id.web_control_exit:
                System.exit(0);
                break;
            case R.id.bt_show_qq_video:
                if (idel_layout != null && mParentView.findViewById(R.id.idel_layout) != null){
                    mParentView.removeView(idel_layout);
                }
                if (isEnableX5Engine){
                    openWebUrlByX5("https://v.qq.com");
                }else{
                    openWebUrlBySystem("https://v.qq.com");
                }
                break;
            case R.id.bt_show_bilibili_video:
                if (idel_layout != null && mParentView.findViewById(R.id.idel_layout) != null){
                    mParentView.removeView(idel_layout);
                }
                if (isEnableX5Engine){
                    openWebUrlByX5("http://www.bilibili.com");
                }else{
                    openWebUrlBySystem("http://www.bilibili.com");
                }
                break;
            default:break;
        }
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

            setIdelView();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setIdelView() {
        if (idel_layout != null){
            mParentView.removeAllViews();
            mParentView.addView(idel_layout);
        }
    }

    @Override
    public void onBackPressed() {
       AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setIcon(R.mipmap.ic_launcher).setTitle("提示").setMessage("是否退出应用?").setNegativeButton("否", new DialogInterface.OnClickListener() {
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
