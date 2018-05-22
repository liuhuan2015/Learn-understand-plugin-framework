package com.liuh.dynamic_proxy_hook.hook_context_startActivity;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Date: 2018/5/22 08:10
 * Description:
 * 因为Instrumentation是一个class，不是一个interface，而jdk动态代理只支持接口，所以这里使用静态代理，覆盖掉原始的方法
 */

public class EvilInstrumentation extends Instrumentation {

    private static final String TAG = "EvilInstrumentation";

    Instrumentation base;

    public EvilInstrumentation(Instrumentation base) {
        this.base = base;
    }

    //execStartActivity还有一个target为String类型，其它参数一致的方法
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        // Hook之前, XXX到此一游!
        Log.e(TAG, "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                "\ntarget = [" + target + "], \nintent = [" + intent +
                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

        //开始调用原始的方法，调不调用随你，但是不调用的话，所有的startActivity都会失效，
        //由于这个方法是隐藏的，因此需要使用反射调用；首先找到这个方法

        try {
            Method execStartActivityMethod = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class,
                    IBinder.class,
                    IBinder.class,
                    Activity.class,
                    Intent.class,
                    int.class,
                    Bundle.class
            );

            execStartActivityMethod.setAccessible(true);

            return (ActivityResult) execStartActivityMethod.invoke(base, who, contextThread, token,
                    target, intent, requestCode, options);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //有的rom可能做了修改，需要自己进行适配
            throw new RuntimeException("do not support ! please adapter it");
        }
        return null;
    }

}
