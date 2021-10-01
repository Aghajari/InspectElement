package com.aghajari.app.inspect;

import android.content.Context;

import com.aghajari.app.inspect.utils.AppColors;

public class Application extends android.app.Application {
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        AppColors.init();
    }
}
