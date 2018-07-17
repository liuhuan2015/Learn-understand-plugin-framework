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

![Service的生命周期时序图](https://github.com/liuhuan2015/Learn-understand-plugin-framework/blob/master/service-management/images/service_life.png)<br>

从图中可以看出，Service的生命周期相当简单：整个生命周期从调用 onCreate() 开始起，到 onDestroy() 返回时结束。<br>
对于非绑定服务，就是从startService调用到stopService或者stopSelf调用。对于绑定服务，就是bindService调用到unbindService调用。<br>

如果要手动控制Service组件的生命周期，我们只需要模拟出这个过程即可；而实现这一点并不复杂。<br>

1 . 如果以startService的方式启动插件Service，直接回调要启动的Service对象的onStartCommand方法即可；如果用stopService或者stopSelf的方式停止Service，只需要回调对应的Service组件的onDestroy方法。<br>

2 . 如果用bindService方式绑定插件Service，可以调用对应Service对应的onBind方法，获取onBind方法返回的Binder对象，然后通过ServiceConnection对象进行回调统计；unBindService的实现同理。<br>

##### 3.1  完全手动控制
我们必须在startService,stopService等方法被调用的时候拿到控制权，才能手动去控制Service的生命周期；要达到这一目的非常简单——Hook ActivityManageNative即可。在Activity的插件化方案中我们就通过这种方式接管了startActivity调用。<br>

Hook掉ActivityManageNative之后，可以拦截对于startService以及stopService等方法的调用；拦截之后，我们可以直接对插件Service进行操作：

1 . 拦截到startService之后，如果Service还没有创建就直接创建Service对象(可能需要加载插件)，然后调用这个Service的onCreate，onStartCommond方法；如果Service已经创建，获取到创建的Service对象并执行其onStartCommond方法。<br>

2 . 拦截到stopService之后，获取到对应的Service对象，直接调用这个Service的onDestory方法。<br>

这种方案看起来简单的让人不敢相信，但是很可惜，这么干是不行的。<br>

首先，Service存在的意义在于它作为一个后台任务，拥有相对较高的运行时优先级；除非在内存极其不足威胁到前台Activity的时候，这个组件才会被系统杀死。<br>
上述这种实现完全把Service当作一个普通的Java对象使用了，因此并没有完全实现Service所具备的能力。<br>

其次，Activity以及Service等组件是可以指定进程的，而让Service运行在某个特定进程的情况非常常见——所谓的远程Service；用上述这种办法压根儿没有办法让某个Service对象运行在一个别的进程。<br>
Android系统给开发者控制进程的机会太少了，要么在AndroidManifest.xml中通过process属性指定，要么借助Java的Runtime类或者native的fork；这几种方式都无法让我们以一种简单的方式配合上述方案达到目的。<br>

##### 3.2  代理分发技术
既然我们希望插件的Service具有一定的运行时优先级，那么一个货真价实的Service组件是必不可少的——只有这种被系统认可的真正的Service组件才具有所谓的运行时优先级。<br>

因此，我们可以注册一个真正的Service组件ProxyService，让这个Service承载一个真正的Service组件所具备的能力（进程优先级等）；<br>
当启动插件的服务比如PluginService的时候，我们统一启动这个ProxyService，当这个ProxyService运行起来之后，再在它的onStartCommand等方法里面进行分发，执行PluginService的onStartCommond等对应的方法；<br>
我们把这种方案形象地称为「代理分发技术」。<br>

代理分发技术也可以完美解决插件Service可以运行在不同的进程的问题——我们可以在AndroidManifest.xml中注册多个ProxyService，指定它们的process属性，让它们运行在不同的进程；<br>
当启动的插件Service希望运行在一个新的进程时，我们可以选择某一个合适的ProxyService进行分发。<br>
也许有童鞋会说，那得注册多少个ProxyService才能满足需求啊？理论上确实存在这问题，但事实上，一个App使用超过10个进程的几乎没有；因此这种方案是可行的。<br>

#### 四 . Service插件化方案的具体实现
##### 1 . 注册代理Service
需要一个货真价实的Service组件来承载进程优先级等功能，因此需要在AndroidManifest.xml中声明一个或者多个（用以支持多进程）这样的Sevice。
##### 2 . 拦截startService等调用过程
要手动控制Service组件的声明周期，需要拦截startService,stopService等调用，并把启动插件Service全部重定向为启动ProxyService（保留原始插件Service信息）；<br>
这个拦截过程需要Hook ActivityManageNative.<br>

在收到startService,stopService之后可以进行具体的操作，对于startService来说，就是直接替换启动的插件Service为ProxyService等待后续处理.<br>

对stopService的处理略有不同但是大同小异.<br>
##### 3 . 分发Service
Hook ActivityManageNative之后，所有的插件Service的启动都被重定向到了我们注册的ProxyService，这样可以保证我们的插件Service有一个真正的Service组件作为宿主；<br>

但是要执行特定插件Service的任务，我们必须把这个任务分发到真正要启动的Service上去；以onStart为例，在启动ProxyService之后，会收到ProxyService的onStart回调，<br>

我们可以在这个方法里面把具体的任务交给原始要启动的插件Service组件.<br>
##### 4 . 加载Service
我们可以在ProxyService里面把任务转发给真正要启动的插件Service组件，要完成这个过程需要创建一个对应的插件Service对象，比如PluginService；<br>

但是通常情况下插件存在于单独的文件之中，正常的方式是无法创建这个PluginService对象的，宿主程序默认的ClassLoader无法加载插件中对应的这个类；<br>

所以，要创建这个对应的PluginService对象，必须先完成插件的加载过程，让这个插件中的所有类都可以被正常访问;<br>

这种技术在前面讨论过，并给出了「激进方案」和「保守方案」，详见插件加载机制。Droid Plugin中采用的是激进方案。<br>
##### 5 . 匹配过程
我们把启动插件中的Service重定向为启动ProxyService，现在ProxyService已经启动，因此必须把控制权交回给原始的PluginService；<br>

在加载插件的时候，我们存储了插件中所有的Service组件的信息，因此，只需要根据Intent里面的Component信息就可以取出对应的PluginService。
##### 6 . 创建以及分发
插件被加载之后，我们就需要创建插件Service对应的Java对象了；由于这些类是在运行时动态加载进来的，肯定不能直接使用new关键字——我们需要使用反射机制。<br>

下面的代码创建出的插件Service对象能满足要求吗？<br>
```java
    ClassLoader cl = getClassLoader();
    Service service = cl.loadClass("com.plugin.xxx.PluginService1");
```
Service作为Android系统的组件，最重要的特点是它具有Context；所以，直接通过反射创建出来的这个PluginService就是一个壳子——没有Context的Service能干什么？<br>

因此我们需要给将要创建的Service类创建出Conetxt；但是Context应该如何创建呢？我们平时压根儿没有这么干过，Context都是系统给我们创建好的。<br>

既然这样，我们可以参照一下系统是如何创建Service对象的；<br>

系统创建Service对象的过程发生在ActivityThread类的handleCreateService方法中，摘要如下：<br>
```java
 try {
            java.lang.ClassLoader cl = packageInfo.getClassLoader();
            service = (Service) cl.loadClass(data.info.name).newInstance();
        } catch (Exception e) {
            if (!mInstrumentation.onException(service, e)) {
                throw new RuntimeException(
                    "Unable to instantiate service " + data.info.name
                    + ": " + e.toString(), e);
            }
        }
        
   try {
             if (localLOGV) Slog.v(TAG, "Creating service " + data.info.name);
 
             ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
             context.setOuterContext(service);
 
             Application app = packageInfo.makeApplication(false, mInstrumentation);
             service.attach(context, this, data.info.name, data.token, app,
                     ActivityManager.getService());
             service.onCreate();
             mServices.put(data.token, service);
             try {
                 ActivityManager.getService().serviceDoneExecuting(
                         data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
             } catch (RemoteException e) {
                 throw e.rethrowFromSystemServer();
             }
         } catch (Exception e) {
             if (!mInstrumentation.onException(service, e)) {
                 throw new RuntimeException(
                     "Unable to create service " + data.info.name
                     + ": " + e.toString(), e);
             }
         }
```
可以看到，系统也是通过反射创建出了对应的Service对象，然后也创建了对应的Context，并给Service注入了活力。<br>

如果我们模拟系统创建Context这个过程，势必需要进行一系列反射调用，那么我们何不直接反射handleCreateService方法呢？<br>

handleCreateService这个方法并没有把创建出来的Service对象作为返回值返回，而是存放在ActivityThread的成员变量mService之中，这个是小case，我们反射取出来就行。<br>

当我们创建出对应的PluginService，并且拥有至关重要的Context对象时；接下来就可以把消息分发给原始的PluginService组件了，这个分发的过程很简单，直接执行消息对应的回调（onStart，onDestroy等）即可；<br>

至此，算是实现了Service组件的插件化。<br>

#### 五 . 总结
1 . 本文以绑定服务为例分析了Service组件的工作原理，并指出用户交互导致组件生命周期的变化是Activity和Service的根本差别，这种差别使得插件方案对于它们必须采用不同的处理方式；<br>

最后我们通过手动控制Service组件的生命周期结合「代理分发技术」成功的实现了Service组件的插件化；<br>

这种插件化方案堪称「完美」,如果非要吹毛求疵，那只能说由于同一个进程的所有Service都挂载在同一个ProxyService上面，如果系统可用内存不足必须回收Service时，杀死一个ProxyService会导致一大票的插件Service歇菜。<br>


2 . 实际使用过程中，Service组件的更新频度并不高，因此直接把插件Service注册到主程序也是可以接受的；<br>

而且如果需要绑定远程Service，完全可以使用一个Service组件根据不同的Intent返回不同的IBinder，所以不实现Service组件的插件化也能满足工程需要。<br>

值得一提的是，我们对于Service组件的插件化方案实际上是一种「代理」的方式，用这种方式也能实现Activity组件的插件化，有一些开源的插件方案比如 DL 就是这么做的。<br>

3 . 加上以前的文章，我们讲述了Activity，BroadcastReceiver，Service的插件化方式，不知读者思考过没有，实现插件化的关键点在哪里？<br>

Service，Activity等不过就是一些普通的Java类，它们之所以被称作四大组件，是因为它们有生命周期；这也是简单的采用Java的动态加载技术无法实现插件化的原因——动态加载进来的Service等类如果没有生命周期，无异于一个没有灵魂的傀儡。<br>

对于Activity组件来说，由于他的生命周期受用户交互影响，只有系统本身才能对这种交互具有全局掌控力，因此他的插件化方式是 Hook AMS，但是生命周期依然交由系统管理；<br>

而Service以及BroadcastReceiver的生命周期没有额外的因素影响，因此我们选择了手动控制其生命周期的方式。不论是借尸还魂还是女娲造人，对这些组件的插件化归根结底是要赋予组件"生命"。<br>














 






















