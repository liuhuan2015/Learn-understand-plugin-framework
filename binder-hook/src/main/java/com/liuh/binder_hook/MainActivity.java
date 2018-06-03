package com.liuh.binder_hook;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 想hook掉IBinder类里面的queryLocalInterface方法，使其返回我们自定义的代理对象
 * 目的：长按进行粘贴之后，剪切版的内容永远都是you are hooked。
 * 这个是照着示例代码抄的，但是不生效，运行实例代码也不生效。大概是因为android系统版本不一致的原因吧，不知道。
 */
public class MainActivity extends AppCompatActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            BinderHookHelper.hookClipboardService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        getApplicationContext().getSystemService(ACTIVITY_SERVICE);

    }
}
