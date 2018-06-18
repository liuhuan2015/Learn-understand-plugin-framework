package com.liuh.intercept_activity.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.liuh.intercept_activity.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by huan on 2018/6/18.
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = "IActivityManagerHandler";

    private Object mBase;

    public IActivityManagerHandler(Object mBase) {
        this.mBase = mBase;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            //只拦截这个方法
            //替换参数
            //找到参数里面的第一个Intent对象
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
            //替身Activity的包名
            String stubPackage = "com.liuh.intercept_activity";

            //这里我们把启动的Activity临时替换成StubActivity
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            newIntent.setComponent(componentName);

            //把原始要启动的TargetActivity先存起来
            newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, raw);

            //替换掉Intent，达到欺骗的目的
            args[index] = newIntent;

            Log.d(TAG, "hook success");
            return method.invoke(mBase, args);
        }

        return method.invoke(mBase, args);
    }
}
