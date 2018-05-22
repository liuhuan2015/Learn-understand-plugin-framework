package com.liuh.dynamic_proxy_hook.hook_context_startActivity;

import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Date: 2018/5/22 16:27
 * Description:Hook帮助类
 * <p>
 * startActivity有两种流程，一种是Context.startActivity(...),一种是Activity.startActivity(...)
 * 两者的调用连是不一致的
 * <p>
 * Context的实现实际上是ContextImpl的，我们看ContextImpl中startActivity的实现，其中有一段代码如下：
 * mMainThread.getInstrumentation().execStartActivity(
 * getOuterContext(), mMainThread.getApplicationThread(), null,
 * (Activity) null, intent, -1, options);
 * <p>
 * 因为mMainThread是主线程，而主线程一个进程只有一个，因此这是一个良好的hook点
 * 我们写一个Instrumentation的代理类，在其中执行execStartActivity之前打印一个我们的Log
 * <p>
 * 1.寻找Hook点，原则是静态变量或者单例对象，尽量Hook public的对象和方法，非public的不保证每个版本都一样，需要适配
 * 2.选择合适的代理方式，如果是接口可以使用动态代理；如果是类，可以手动写代理或者使用cglib
 * 3.偷梁换柱：用代理对象替换原始对象。
 */

public class HookHelper {

    public static void attachContext() throws Exception {

        //先获取到ActivityThread（主线程）对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        //因为方法是静态的，所以实例对象可以为null，并且没有参数
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        //拿到Instrumentation实例对象
        Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);
        Instrumentation instrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

        //创建代理对象,然后把currentActivityThread中的Instrumentation替换成代理对象
        EvilInstrumentation evilInstrumentation = new EvilInstrumentation(instrumentation);
        mInstrumentationField.set(currentActivityThread, evilInstrumentation);
    }

}
