package com.liuh.dynamic_proxy_hook.hook_activity_startActivity;

import android.app.Activity;
import android.app.Instrumentation;

import java.lang.reflect.Field;

/**
 * Created by huan on 2018/5/23.
 * 想hook掉Activity.startActivity(...)方法，我们需要获取当前Activity的mInstrumentation属性对象
 */

public class HookHelper_Activity {

    public static void attachContext(Activity activity) {

        Class<?> currentActivityClass = Activity.class;

        //先获取当前Activity的mInstrumentation属性对象，因为mInstrumentation属性是Activity的私有属性，
        //所以只能由Activity.class来获取
        Field instrumentationField = null;
        try {
            instrumentationField = currentActivityClass.getDeclaredField("mInstrumentation");

            instrumentationField.setAccessible(true);

            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(activity);
            //进行替换
            EvilActivityInstrumentation evilActivityInstrumentation = new EvilActivityInstrumentation(instrumentation);
            instrumentationField.set(activity, evilActivityInstrumentation);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
