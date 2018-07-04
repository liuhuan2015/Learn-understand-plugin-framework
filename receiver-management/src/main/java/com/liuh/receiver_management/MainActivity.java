package com.liuh.receiver_management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

/**
 * 1 . 先把要加载的文件从assets目录复制到/data/user/0/com.liuh.receiver_management/files/下面<br>
 * 2 . 解析出插件中所有注册的静态广播，然后采用动态广播的方式全部重新注册一遍<br>
 * 3 . 点击按钮后，我们会发送一个广播，在插件中收到这个广播后，按照事前写好的代码，它会回发一个广播，如果这个流程顺利走完，则表示插件中的静态广播已经成功被注册到AMS中了，即我们完成了静态广播的插件化。
 */
public class MainActivity extends AppCompatActivity {
    // 发送广播到插件之后, 插件如果受到, 那么会回传一个ACTION 为这个值的广播;
    static final String ACTION = "com.weishu.upf.demo.app2.PLUGIN_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Button button = new Button(this);
        setContentView(button);
        button.setText("send broadcast to plugin: demo");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "插件插件!收到请回答!!", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("com.weishu.upf.demo.app2.Receiver1"));
            }
        });

        Utils.extractAssets(this, "test.jar");
        File testPlugin = getFileStreamPath("test.jar");
        try {
            ReceiverHelper.preLoadReceiver(this, testPlugin);
            Log.e(getClass().getSimpleName(), "hook success");
        } catch (Exception e) {
            throw new RuntimeException("receiver load failed", e);
        }

        // 注册插件收到我们发送的广播之后, 回传的广播
        registerReceiver(mReceiver, new IntentFilter(ACTION));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "插件插件,我是主程序,握手完成!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
