#### 一 . 前言
相对于实现Activity的插件化，BroadcastReceiver的插件化实现要简单很多——BroadcastReceiver的生命周期相当简单。<br>

我们平时使用BroadcastReceiver的操作包含：注册，发送，接收；因此，要实现BroadcastReceiver的插件化就这三种操作提供支持。<br>

#### 二 . 源码分析
开发中，我们可以注册一个BroadcastReceiver，然后接收我们感兴趣的广播；也可以给某一个有缘人发出某个广播。<br>

##### 1 . 注册过程
不论是静态广播还是动态广播，在使用之前都是需要注册的；静态广播的注册是直接在AndroidManifest.xml中声明；动态广播的注册则需要借助Context类的registReceiver方法。<br>

动态注册BroadcastReceiver的源码分析：Context类的registerReceiver真正实现是在ContextImpl中,最终会调用到ContextImpl类中的registerReceiverInternal方法<br>
下面是registerReceiverInternal方法（android-27）
```java
   private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId,
            IntentFilter filter, String broadcastPermission,
            Handler scheduler, Context context, int flags) {
        IIntentReceiver rd = null;
        if (receiver != null) {
            if (mPackageInfo != null && context != null) {
                if (scheduler == null) {
                    scheduler = mMainThread.getHandler();
                }
                rd = mPackageInfo.getReceiverDispatcher(
                    receiver, context, scheduler,
                    mMainThread.getInstrumentation(), true);
            } else {
                if (scheduler == null) {
                    scheduler = mMainThread.getHandler();
                }
                rd = new LoadedApk.ReceiverDispatcher(
                        receiver, context, scheduler, null, true).getIIntentReceiver();
            }
        }
        try {
            final Intent intent = ActivityManager.getService().registerReceiver(
                    mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
                    broadcastPermission, userId, flags);
            if (intent != null) {
                intent.setExtrasClassLoader(getClassLoader());
                intent.prepareToEnterProcess();
            }
            return intent;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
BroadcastReceiver的注册也是通过AMS完成的；IIntentReceiver类型的变量rd是一个通过aidl工具生成的Binder对象,可以用来跨进程通信；<br>
去grepcode上看，文档显示：IIntentReceiver类型的变量rd是用来进行广播分发的。<br>

因为广播的分发过程是在AMS中进行的，而AMS所在的进程和BroadcastReceiver所在的进程不一样，因此要把广播分发到BroadcastReceiver的进程需要跨进程通信，这个通信的载体就是IIntentReceiver类。<br>
这个类的作用跟前面Activity生命周期管理中提到的IApplicationThread的作用相同，都是App进程给AMS进程用来进行通信的对象。另外，IIntentReceiver是一个接口，它的实现类是LoadedApk.ReceiverDispatcher。<br>

AMS类的registerReceiver方法代码有点多，主要做了以下两件事：<br>
1 . 对发送者的身份和权限做出一定的校验。<br>
2 . 把这个BroadcastReceiver以BroadcastFilter的形式存储在AMS的mReceiverResolver变量中，供后续使用。<br>

就这样，被传递过来的BroadcastReceiver已经成功的注册在系统之中，能够接收特定类型的广播了。<br>

在插件加载机制中，我们知道系统会通过PackageParser解析apk中的AndroidManifest.xml文件，因此我们有理由认为：系统会在解析AndroidManifest.xml的<receiver>标签（也即静态注册的广播）的时候保存相应的信息；<br>
而apk的解析过程是在PMS中进行的，因此静态注册广播的信息存储在PMS中。

##### 2 . 发送和接收过程
 发送广播很简单，直接context.sendBroadcast(),具体的实现在ContextImpl中
 ```java
   @Override
     public void sendBroadcast(Intent intent) {
         warnIfCallingFromSystemProcess();
         String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
         try {
             intent.prepareToLeaveProcess(this);
             ActivityManager.getService().broadcastIntent(
                     mMainThread.getApplicationThread(), intent, resolvedType, null,
                     Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                     getUserId());
         } catch (RemoteException e) {
             throw e.rethrowFromSystemServer();
         }
     }
 ```
 我们发现：发送广播也是通过AMS进行的，在AMS源码中，AMS的broadcastIntent方法调用了broadcastIntentLocked方法，在其中处理了诸如粘性广播、顺序广播、各种flag以及动态广播静态广播的接收过程；<br>
 值得注意的是：在这个方法中可以看到，广播的发送和接收是融为一体的。某个广播被发送后，AMS会找出所有的注册过的BroadcastReceiver中与这个广播匹配的接收者，然后将这个广播分发给相应的接收者处理。<br>
 
 在AMS的broadcastIntentLocked方法中，通过PMS拿到所有符合要求的静态BroadcastReceiver，然后从AMS中拿到所有符合要求的动态BroadcastReceiver；<br>
 然后接下来的工作比较简单：唤醒这些广播接收者，简单来说就是回调它们的onReceive方法。<br>
 
 往后的源码分析过程不抄了。
 
 #### 三 . 思路分析——怎么才能实现对BroadcastReceiver的插件化？
 
 
 
 
 

 
