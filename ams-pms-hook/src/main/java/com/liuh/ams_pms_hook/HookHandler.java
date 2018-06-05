package com.liuh.ams_pms_hook;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by huan on 2018/6/3.
 */

public class HookHandler implements InvocationHandler {

    private static final String TAG = "HookHandler";

    private Object mBase;

    public HookHandler(Object mBase) {
        this.mBase = mBase;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.e(TAG, "hey hey hey, baby; you are hooked!!-----我们可以在此处做一些操作");
        Log.e(TAG, "method:" + method.getName() + " called with args:" + Arrays.toString(args));
        return method.invoke(mBase, args);
    }
}
