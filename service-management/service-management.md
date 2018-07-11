Android插件化原理解析——Service的插件化
#### 一 . Service的工作原理
Service分为两种：以startService启动的服务和用bindService绑定的服务；这两个过程大体相似，bindService稍微复杂一些。<br>
```java
 public boolean bindService(Intent service, ServiceConnection conn,
            int flags){...} 
```
bindService有三个参数：第一个参数表示要绑定的Service的Intent；第二个参数是一个ServiceConnection，我们可以通过这个对象接收到Service绑定成功或者失败的回调；<br>
第三个参数则是绑定时候的一些FLAG。<br>

Context的具体实现在ContextImpl中,ContextImpl中的bindService方法直接调用了bindServiceCommon方法，源码如下（android-27）<br>
```java
    private boolean bindServiceCommon(Intent service, ServiceConnection conn, int flags, Handler
            handler, UserHandle user) {
        // Keep this in sync with DevicePolicyManager.bindDeviceAdminServiceAsUser.
        IServiceConnection sd;
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        }
        if (mPackageInfo != null) {
            sd = mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
        } else {
            throw new RuntimeException("Not supported in system context");
        }
        validateServiceIntent(service);
        try {
            IBinder token = getActivityToken();
            if (token == null && (flags&BIND_AUTO_CREATE) == 0 && mPackageInfo != null
                    && mPackageInfo.getApplicationInfo().targetSdkVersion
                    < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags |= BIND_WAIVE_PRIORITY;
            }
            service.prepareToLeaveProcess(this);
            int res = ActivityManager.getService().bindService(
                mMainThread.getApplicationThread(), getActivityToken(), service,
                service.resolveTypeIfNeeded(getContentResolver()),
                sd, flags, getOpPackageName(), user.getIdentifier());
            if (res < 0) {
                throw new SecurityException(
                        "Not allowed to bind to service " + service);
            }
            return res != 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
这个方法最终通过ActivityManager借助AMS进而完成了Service的绑定过程。IServiceConnection sd，这个变量和IApplicationThread以及前面广播的插件化管理里面遇到的IIntentReceiver相同，<br>
都是ActivityThread给AMS提供的用来与之进行通信的Binder对象；这个接口的实现类是LoadedApk.ServiceDispatcher。<br>
这个方法最终调用了ActivityManagerNative的bindService，而这个方法的真正实现在AMS里面。<br>

Service的创建过程在ActivityThread中，通过handleCreateService方法，整个创建过程和Activity的创建过程如出一辙；但是两者的创建过程还是略有不同的，<br>
虽然都是通过ClassLoader + 反射创建，但是Activity却把创建过程委托给了Instrumentation类，而Service则是直接进行。<br>

在这里作者沿着源码进行了一系列的分析，觉得有点长，看的昏昏欲睡~就暂时先略过吧<br>

#### 二 . Service的插件化思路
从上面的源码分析来看，Service组件与Activity组件有着非常多的相似之处：<br>
它们都是通过Context类完成启动，接着通过ActivityManageNative进入AMS，最后又通过IApplicationThread这个Binder IPC到App进程的Binder线程池，然后通过 H 转发消息到App进程的主线程，<br>
最终完成生命周期的回调；<br>
对于Service组件，看起来好像可以沿用Activity组件的插件化方式：Hook掉ActivityManageNative以及 H 类，但是事实真的是可以这样吗？<br>

##### 1.Service与Activity的异同

**用户交互对于生命周期的影响**<br>
首先，Activity与Service组件最大的不同点在于，Activity组件可以与用户进行交互；这一点意味着用户的行为会对Activity组件产生影响，对我们来说最重要的影响就是Activity组件的生命周期；<br>
用户点击按钮从界面A跳转到界面B，会引起A和B这两个Activity一系列生命周期的变化。而Service组件则代表后台任务，除了内存不足系统回收之外，它的生命周期完全由我们的代码控制，与用户的交互无关。<br>

这意味着什么呢？<br>

Activity组件的生命周期受用户交互影响，而这种变化只有Android系统才能感知，因此我们必须把插件的Activity交给系统管理，才能拥有完整的生命周期；但Service组件的生命周期不受外界因素影响，<br>
那么自然而然，我们可以**手动控制它的生命周期**，就像我们对于BroadcastReceiver的插件化方式一样！Activity组件的插件化无疑是比较复杂的，为了把插件Activity交给系统管理进而拥有完整生命周期，<br>
我们设计了一个天衣无缝的方案骗过了AMS；既然Service的生命周期可以由我们自己控制，那么我们可以有更简单的方案实现它的插件化。<br>

**Activity的任务栈**<br>
虽然Activity的插件化技术更复杂，但是这种方案并不能完成Service组件的插件化——复杂的方案并不意味着它能处理更多的问题。<br>

原因在于Activity拥有任务栈的概念，任务栈是Service组件与Activity组件插件化方式分道扬镳的根本原因。<br>

任务栈的概念使得Activtiy的创建就代表着入栈，销毁则代表出栈；又由于Activity代表着与用户交互的界面，所以这个栈的深度不可能太深——Activity栈太深意味着用户需要狂点back键才能回到初始界面，这种体验显然有问题；<br>
因此，插件框架要处理的Activity数量其实是有限的，所以我们在AndroidManifest.xml中声明有限个StubActivity就能满足插件启动近乎无限个插件Activity的需求。<br>

但是Service组件不一样，理论情况下，可以启动的Service组件是无限的——除了硬件以及内存资源，没有什么限制它的数目；如果采用Activity的插件化方式，就算我们在AndroidMafenist.xml中声明再多的StubService，<br>
总有不能满足插件中要启动的Service数目的情况出现。也许有童鞋会说，可以用一个StubService对应多个插件Service，这确实能解决部分问题；但是，下面的这个区别让这种设想彻底泡汤。<br>

**Service无法拥有多实例**<br>

Service组件与Activity组件另外一个不同点在于，对同一个Service调用多次startService并不会启动多个Service实例，而非特定Flag的Activity是可以允许这种情况存在的，因此如果用StubService的方式，<br>
为了实现Service的这种特性，必须建立一个StubService到插件Service的一个Map，Map的这种一一对应关系使得我们使用一个StubService对应多个插件Service的计划成为天方夜谭。<br>

结论：**对于Service组件的插件化，我们不能简单地套用Activity的方案**

#### 三 . 如何实现Service的插件化？
上文指出，我们不能套用Activity的方案实现Service组件的插件化，但是可以通过手动控制Service组件的生命周期实现；<br>
Service的生命周期图如下：<br>
![Service的生命周期时序图](https://github.com/liuhuan2015/Learn-understand-plugin-framework/blob/master/service-management/images/service_life.png)

















