package com.sunteng.x5webenginedemo;

import android.app.Application;
import android.content.Intent;

/**
 * X5WebEngineDemo Created by baishixian on 2017/1/19.
 */

public class X5Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initX5();
    }

    private void initX5() {
        Intent intent = new Intent(this, AdvanceLoadX5Service.class);
        startService(intent);
    }
}
