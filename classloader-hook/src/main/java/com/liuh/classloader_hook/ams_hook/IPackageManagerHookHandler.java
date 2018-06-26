package com.liuh.classloader_hook.ams_hook;

import android.content.pm.PackageInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Date: 2018/6/26 11:27
 * Description:
 */

public class IPackageManagerHookHandler implements InvocationHandler {

    private Object mBase;

    public IPackageManagerHookHandler(Object mBase) {
        this.mBase = mBase;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
