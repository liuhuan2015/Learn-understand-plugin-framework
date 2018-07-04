package com.liuh.receiver_management;

import android.app.Application;
import android.content.Context;

/**
 * Date: 2018/7/4 15:44
 * Description:
 */

public class UPFApplication extends Application {

    private static Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mContext = base;
    }

    public static Context getContext() {
        return mContext;
    }
}
