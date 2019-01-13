package com.example.jbois.go4lunch.Utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class ApplicationContext extends Application {
    private static Application instance;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getBaseContext();
    }
}
