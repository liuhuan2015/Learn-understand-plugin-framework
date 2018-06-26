package com.liuh.classloader_hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.liuh.classloader_hook.ams_hook.AMSHookHelper;
import com.liuh.classloader_hook.classloader_hook.BaseDexClassLoaderHookHelper;

import java.io.File;

/**
 * android插件加载机制大概有两种，一种是使用系统的classloader(保守方案，又称单classloader方案)，
 * 一种是使用自定义的classloader替换掉系统的classloader(激进方案，又称多classloader方案)
 * <p>
 * 方案一(使用系统的classloader(保守方案，又称单classloader方案))原理：<br/>
 * 1.默认情况下，performLaunchActivity会使用替身StubActivity的ApplicationInfo也就是宿主程序的ClassLoader加载所有的类；
 * 我们的思路是告诉宿主ClassLoader我们在哪，让其帮忙完成类加载的过程。
 * <p>
 * 2.宿主程序的ClassLoader最终继承自BaseDexClassLoader，BaseDexClassLoader通过DexPathList进行类的查找过程；
 * 而这个查找通过遍历一个dexElements的数组完成；我们通过把插件dex添加进这个数组就可以让宿主ClassLoader具有获取插件类的能力。
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int PATCH_BASE_CLASS_LOADER = 1;//打补丁(保守方案，使用系统的classloader)

    private static final int CUSTOM_CLASS_LOADER = 2;//自定义ClassLoader（激进方案）

    private static final int HOOK_METHOD = PATCH_BASE_CLASS_LOADER;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      setContentView(R.layout.activity_main);

        Button t = new Button(this);
        setContentView(t);

        Log.d(TAG, "context classloader : " + getApplicationContext().getClassLoader());

        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (HOOK_METHOD == PATCH_BASE_CLASS_LOADER) {
                    //启动一个位于插件中的Activity
                    intent.setComponent(new ComponentName("com.liuh.dynamic_proxy_hook",
                            "com.liuh.dynamic_proxy_hook.MainActivity"));
                } else {
                    intent.setComponent(new ComponentName("", ""));
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        try {
            Utils.extractAssets(newBase, "dynamic-proxy-hook.apk");//把Assets里面的文件复制到/data/data/<package>/files目录下
//            Utils.extractAssets(newBase, "ams-pms-hook.apk");
//            Utils.extractAssets(newBase, "test.apk");

            if (HOOK_METHOD == PATCH_BASE_CLASS_LOADER) {
                File dexFile = getFileStreamPath("dynamic-proxy-hook.apk");//获取/data/data/<package>/files目录下的文件
                File optDexFile = getFileStreamPath("dynamic-proxy-hook.dex");
                BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), dexFile, optDexFile);//把文件添加进系统classloader的pathList的dexElements数组中
            } else {
                //            LoadedApkClassLoaderHookHelper.hookLoadedApkInActivityThread(getFileStreamPath("ams-pms-hook.apk"));
            }
            AMSHookHelper.hookActivityManagerNative();
            AMSHookHelper.hookActivityThreadHandler();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
