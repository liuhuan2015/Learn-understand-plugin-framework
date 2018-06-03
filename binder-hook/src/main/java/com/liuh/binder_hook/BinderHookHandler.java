package com.liuh.binder_hook;

import android.content.ClipData;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huan on 2018/6/3.
 */

public class BinderHookHandler implements InvocationHandler {

    private static final String TAG = "BinderHookHandler";

    Object base;//原始的Service对象(IInterface)

    public BinderHookHandler(IBinder base, Class<?> stubClass) {
        try {
            Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);
            this.base = asInterfaceMethod.invoke(null, base);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //把剪贴板的内容替换成 "you are hooked"
        if ("getPrimaryClip".equals(method.getName())) {
            Log.e("--------", "hook getPrimaryClip");
            return ClipData.newPlainText(null, "you are hooked");
        }

        //欺骗系统，使之认为剪切板上一直有内容
        if ("hasPrimaryClip".equals(method.getName())) {
            return true;
        }

        return method.invoke(base, args);
    }
}
