package com.liuh.classloader_hook.ams_hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.liuh.classloader_hook.StubActivity;
import com.liuh.classloader_hook.UPFApplication;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Date: 2018/6/26 10:56
 * Description:
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = "IActivityManagerHandler";

    Object mBase;

    public IActivityManagerHandler(Object mBase) {
        this.mBase = mBase;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            //只拦截这个方法
            //替换参数，任你所为；甚至替换原始Activity启动别的Activity
            Intent raw;
            int index = 0;

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];

            Intent newIntent = new Intent();

            // 替身Activity的包名, 也就是我们自己的"包名", Application Id, 如果用gradle打包
            String stubPackage = UPFApplication.getContext().getPackageName();

            // 这里我们把启动的Activity临时替换为 StubActivity
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            newIntent.setComponent(componentName);

            // 把我们原始要启动的TargetActivity先存起来
            newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, raw);

            // 替换掉Intent, 达到欺骗AMS的目的
            args[index] = newIntent;

            Log.d(TAG, "hook success");
            return method.invoke(mBase, args);
        }

        return method.invoke(mBase, args);
    }
}
