package com.liuh.intercept_activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liuh.intercept_activity.hook.AMSHookHelper;

/**
 * 如果是继承Activity的话，是可以使用这种方式启动没有在AndroidManifest.xml注册的Activity的。
 * <p>
 * 但是如果继承的是AppCompatActivity的话，会报错。
 * java.lang.RuntimeException: Unable to start activity ComponentInfo{com.liuh.intercept_activity/com.liuh.intercept_activity.TargetActivity}:
 * java.lang.IllegalArgumentException: android.content.pm.PackageManager$NameNotFoundException: ComponentInfo{com.liuh.intercept_activity/com.liuh.intercept_activity.TargetActivity}
 * at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2741)
 *
 * Caused by: java.lang.IllegalArgumentException: android.content.pm.PackageManager$NameNotFoundException
 *
 * Caused by: android.content.pm.PackageManager$NameNotFoundException
 *
 * 感觉大概是系统又调用了pm来查询。
 *
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        try {
            AMSHookHelper.hookActivityManagerNative();
            AMSHookHelper.hookActivityThreadHandler();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_openTarget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TargetActivity.class));
            }
        });

    }
}
