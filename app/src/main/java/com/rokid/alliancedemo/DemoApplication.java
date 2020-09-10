package com.rokid.alliancedemo;

import android.app.Application;

import com.rokid.alliance.base.BaseLibrary;

/**
 * Author: heshun
 * Date: 2020/8/20 2:13 PM
 * gmail: shunhe1991@gmail.com
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BaseLibrary.initialize(this);

    }
}
