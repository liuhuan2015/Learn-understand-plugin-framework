package com.liuh.intercept_activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TargetActivity extends Activity {

    private static final String TAG = TargetActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("我是TargetActivity");
        setContentView(tv);

        Log.e(TAG, "onCreate............");
//        setContentView(R.layout.activity_target);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart............");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume............");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause............");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop............");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy............");
    }
}
