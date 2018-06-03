package com.liuh.binder_hook;

import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by huan on 2018/6/3.
 */

public class BinderProxyHookHandler implements InvocationHandler {
    private static final String TAG = "BinderProxyHookHandler";

    //绝大部分情况下，这是一个BinderProxy对象
    //只有当Service和我们在同一个进程的时候才是Binder本地对象
    //这个基本不可能
    IBinder base;

    Class<?> stub;

    Class<?> iinterface;

    public BinderProxyHookHandler(IBinder base) {
        this.base = base;
        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("queryLocalInterface".equals(method.getName())) {
            Log.d(TAG, "hook queryLocalInterface");
            //这里直接返回真正被hook掉的Service接口
            //这里的queryLocalInterface 就不是原本的意思了
            //我们肯定不会真的返回一个本地接口，因为我们接管了asInterface方法的作用
            //因此必须是一个完整的asInterface过的IInterface对象，既要处理本地对象，也要处理代理对象
            //这只是一个Hook点而已，它原始的含义已经被我们重新定义了；因为我们会永远确保这个方法不返回null
            //让IClipboard.Stub.asInterface永远走到if语句的else分支里面

            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),

                    new Class[]{IBinder.class, IInterface.class, this.iinterface},
                    new BinderHookHandler(base, stub));
        }
        Log.e(TAG, "method:" + method.getName());
        return method.invoke(base, args);
    }
}
