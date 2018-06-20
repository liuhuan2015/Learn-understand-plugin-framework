package com.liuh.intercept_activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.liuh.intercept_activity.hook.AMSHookHelper;

/**
 * 如果是继承Activity的话，是可以使用这种方式启动没有在AndroidManifest.xml注册的Activity的。
 * <p>
 * 但是如果继承的是AppCompatActivity的话，会报错。
 * java.lang.RuntimeException: Unable to start activity ComponentInfo{com.liuh.intercept_activity/com.liuh.intercept_activity.TargetActivity}:
 * java.lang.IllegalArgumentException: android.content.pm.PackageManager$NameNotFoundException: ComponentInfo{com.liuh.intercept_activity/com.liuh.intercept_activity.TargetActivity}
 * at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2741)
 * <p>
 * Caused by: java.lang.IllegalArgumentException: android.content.pm.PackageManager$NameNotFoundException
 * <p>
 * Caused by: android.content.pm.PackageManager$NameNotFoundException
 * <p>
 * 感觉大概是系统又调用了pm来查询。
 * <p>
 * Activity启动过程中很多重要的操作，都不是在app进程里面执行的，而是在AMS所在的系统进程里面执行的，
 * 例如：对Activity是否在AndroidManifest中进行了声明的校验
 * <p>
 * 要想打开一个没有在AndroidManifest中声明的Activity，我们可以这样做：
 * 假装启动一个已经声明过的替身Activity，让这个Activity进入AMS进程接受校验，最后在回调中再换成我们真正要启动的Activity。
 * 这样就欺骗了AMS进程，瞒天过海。
 * <p>
 * AMS进程只知道StubActivity的存在，不知道TargetActivity的存在
 * <p>
 * AMS与ActivityThread之间对于Activity的生命周期的交互，并没有直接使用Activity对象进行交互，而是使用一个token来标识，
 * 这个token是Binder对象，因此可以方便的跨进程传递。Activity里面有一个成员变量mToken代表的就是它，token可以唯一的标识一个Activity对象，
 * 它在Activity的attach方法里面初始化。
 * <p>
 * 在AMS处理Activity的任务栈的时候，使用这个token标记Activity，因此在我们的demo里面，AMS进程里面的token对应的是StubActivity，
 * 即AMS认为它是在操作StubActivity。但是在我们的app进程中，token对应的却是TargetActivity。因此，在ActivityThread执行回调的时候，
 * 能够正确的回调到TargetActivity相应的方法。
 *
 * 我们要完成插件系统中类的加载，可以通过自定义ClassLoader实现。
 *
 * 解决了"启动没有在AndroidManifest中显式声明的，并且存在于外部文件中的Activity"的问题，插件系统对于Activity的管理才算得上是一个完全体。
 */
public class MainActivity extends Activity {

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
