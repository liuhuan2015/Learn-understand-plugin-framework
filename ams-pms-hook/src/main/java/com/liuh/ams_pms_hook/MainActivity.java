package com.liuh.ams_pms_hook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_hook_ams).setOnClickListener(this);
        findViewById(R.id.btn_hook_pms).setOnClickListener(this);
    }

    //这个方法比onCreate调用的早；在这里hook比较好
    @Override
    protected void attachBaseContext(Context newBase) {
        HookHelper.hookActivityManager();
        HookHelper.hookPackageManager(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hook_ams:
                //测试AMS hook
                Uri uri = Uri.parse("http://www.baidu.com");
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setData(uri);
                startActivity(it);
                break;
            case R.id.btn_hook_pms:
                //测试PMS hook（调用其相关方法）
                getPackageManager().getInstalledApplications(0);
                break;
        }
    }
}
