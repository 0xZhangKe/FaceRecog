package com.zhangke.facerecog.common;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.zhangke.zlog.ZLog;

/**
 * Created by ZhangKe on 2018/5/18.
 */
public class App extends MultiDexApplication {

    private static final String TAG = "App";

    private static App application;
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mContext = getApplicationContext();

        ZLog.Init(String.format("%s/log/", getExternalFilesDir(null).getPath()));
    }

    public static App getInstance() {
        return application;
    }
}
