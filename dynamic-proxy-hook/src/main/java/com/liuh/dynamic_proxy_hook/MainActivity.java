package com.liuh.dynamic_proxy_hook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.liuh.dynamic_proxy_hook.hook_activity_startActivity.HookHelper_Activity;

public class MainActivity extends AppCompatActivity {

    Button btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dynamic_proxy_hook);

        HookHelper_Activity.attachContext(this);

        btn_test = findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("http://www.baidu.com"));

//                getApplicationContext().startActivity(intent);
                startActivity(intent);
            }
        });
    }

    //在Activity的attach(...)方法中会调用attachBaseContext方法，所以它会执行在onCreate(...)之前
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
//            HookHelper.attachContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
