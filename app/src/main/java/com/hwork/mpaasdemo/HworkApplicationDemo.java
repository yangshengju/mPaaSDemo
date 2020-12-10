package com.hwork.mpaasdemo;

import android.app.Application;
import android.content.Context;

import com.mpaas.mps.adapter.api.MPPush;

public class HworkApplicationDemo extends android.app.Application {

    private static HworkApplicationDemo _instance;
    public HworkApplicationDemo(){};

    public static HworkApplicationDemo getInstance() {
        if(_instance==null) {
            _instance = new HworkApplicationDemo();
        }
        return _instance;
    }

    public Context getHworkApplicationContext() {
        return getApplicationContext();
    }
    public static Application getApp(){
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        MPPush.setup(this);
        MPPush.init(this);
    }
}
