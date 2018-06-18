package com.liuh.intercept_activity.hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by huan on 2018/6/18.
 */

public class ActivityThreadHandlerCallback implements Handler.Callback {

    Handler mBase;

    public ActivityThreadHandlerCallback(Handler mBase) {
        this.mBase = mBase;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.e("-------", "msg.what : " + msg.what);
        switch (msg.what) {
            //ActivityThread里面"LAUNCH_ACTIVITY"这个字段的值是100
            //本来使用反射的方式获取最好，这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        //这里为了简单起见，直接取出TargetActivity
        Object obj = msg.obj;

        try {
            //把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);

            Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
            raw.setComponent(target.getComponent());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
}
