package com.sunteng.x5webenginedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

/**
 * X5WebEngineDemo Created by baishixian on 2017/1/20.
 */
public class AdvanceLoadX5Service extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initX5();
    }

    private void initX5() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        //TbsDownloader.needDownload(getApplicationContext(), false);

        QbSdk.PreInitCallback preInitCallback = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                Log.e("AdvanceLoadX5Service", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
                Log.e("AdvanceLoadX5Service", " onCoreInitFinished");
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d("AdvanceLoadX5Service","onDownloadFinish");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.d("AdvanceLoadX5Service","onInstallFinish");
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d("AdvanceLoadX5Service","onDownloadProgress:"+i);
            }
        });

        QbSdk.initX5Environment(getApplicationContext(),  preInitCallback);

        Log.d("AdvanceLoadX5Service","X5预加载中...");
    }
}
