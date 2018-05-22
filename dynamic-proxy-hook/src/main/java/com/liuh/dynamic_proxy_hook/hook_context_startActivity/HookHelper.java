package com.liuh.dynamic_proxy_hook.hook_context_startActivity;

/**
 * Date: 2018/5/22 16:27
 * Description:Hook帮助类
 */

public class HookHelper {

    public static void attachContext() throws Exception {

        //先获取到ActivityThread（主线程）对象

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");


    }

}
