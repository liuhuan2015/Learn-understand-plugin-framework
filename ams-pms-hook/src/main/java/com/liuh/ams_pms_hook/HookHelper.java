package com.liuh.ams_pms_hook;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by huan on 2018/6/3.
 * hook ams和pms，使用了反射和动态代理。
 */

public class HookHelper {

    public static void hookActivityManager() {
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");

            //获取gDefault这个字段，想办法替换它
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);

            Object gDefault = gDefaultField.get(null);

            //4.x以上的gDefault是一个android.util.Singleton对象；我们取出这个单例里面的字段
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            //ActivityManagerNative的gDefault对象里面原始的IActivityManager对象
            Object rawIActivityManager = mInstanceField.get(gDefault);

            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},
                    new HookHandler(rawIActivityManager));
            mInstanceField.set(gDefault, proxy);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static void hookPackageManager(Context context) {

        try {
            //获取全局的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            //获取ActivityThread里面原始的sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            //sPackageManager这个Field在ActivityThread中是这样定义的：static volatile IPackageManager sPackageManager;
            //所以我觉得不用传ActivityThread类的实例对象，即传null即可
            //但是原作者不是这样的，他传了ActivityThread的实例对象：currentActivityThread
            //经测试，使用类class和类实例对象都可以
            Object sPackageManagerObj = sPackageManagerField.get(null);

            //准备好代理对象，用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                    new Class<?>[]{iPackageManagerInterface},
                    new HookHandler(sPackageManagerObj));

            //1.替换ActivityThread里面的sPackageManager字段
            sPackageManagerField.set(currentActivityThread, proxy);

            //2.替换ApplicationPackageManager里面的mPm对象
            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, proxy);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


    }

}
