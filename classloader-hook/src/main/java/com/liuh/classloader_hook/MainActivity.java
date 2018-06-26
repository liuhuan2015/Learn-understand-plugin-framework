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

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int PATCH_BASE_CLASS_LOADER = 1;//打补丁(保守方案，使用系统的classloader)

    private static final int CUSTOM_CLASS_LOADER = 2;//自定义ClassLoader（激进方案）

    private static final int HOOK_METHOD = CUSTOM_CLASS_LOADER;


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
            Utils.extractAssets(newBase, "dynamic-proxy-hook.apk");
//            Utils.extractAssets(newBase, "ams-pms-hook.apk");
            Utils.extractAssets(newBase, "test.apk");

            if (HOOK_METHOD == PATCH_BASE_CLASS_LOADER) {
                File dexFile = getFileStreamPath("test.apk");
                File optDexFile = getFileStreamPath("test.dex");
                BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), dexFile, optDexFile);
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
