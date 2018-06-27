# Learn-understand-plugin-framework
>这个项目是为了学习[understand-plugin-framework](https://github.com/tiann/understand-plugin-framework)而创建的，是一个练习项目。
主要包括内容有：
#### 一 . 静态代理和动态代理(jdk动态代理),以及使用代理模式hook系统的startActivity(...),这块还涉及到反射的使用.<br>
使用的是一个现实生活中找代理买东西的例子<br>
##### 1 . 静态代理,可以理解为我们没有时间自己亲自去买东西,于是我们找了一个代购帮我们去买.
 我们把要买什么东西给他说一下（即接口里面的功能方法）,把钱给他（即接口里面的功能方法的参数）.<br>
``` java   
    public class StaticProxyShopping implements IShopping {
    IShopping base;
    public StaticProxyShopping(IShopping base) {
        this.base = base;
    }
    @Override
    public Object[] doShopping(long money) {
        System.out.println(String.format("我收了顾客%s块钱。", money));

        //先黑点钱
        long realCost = (long) (money * 0.5f);

        System.out.println(String.format("但是我实际上花了%s块钱，我去买东西了。", realCost));

        Object[] things = base.doShopping(realCost);

        System.out.println("我买到了" + Arrays.toString(things));

        if (things != null && things.length > 0) {
            things[0] = "被掉包的东西";
        }

        return things;
    }
  }
``` 
##### 2 . 动态代理,传统的静态代理模式需要为每一个被代理的类写一个代理类,如果需要被代理的类有很多,这种方式就不合适了.为了更优雅的实现代理模式,JDK提供了动态代理模式,可以简单理解为JVM可以在运行时帮我们动态生成一系列的代理类,这样我们就不需要手动写每一个静态的代理类了.
 Java动态代理位于java.lang.reflect包下，一般主要涉及到以下两个类：<br>
 （1）Interface InvocationHandler<br>
 该接口中仅定义了一个方法：public Object invoke(Object obj, Method method, Object[] args)，<br>
 在使用时，第一个参数obj一般是指被代理的对象，method是被代理的方法，args为该方法的参数数组。<br>
 这个抽象方法在代理类中动态实现。<br>
 （2）Proxy<br>
 该类即为动态代理类，<br>
 static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)，<br>
 返回代理类的一个实例，返回后的代理类可以当作被代理类使用<br>
 loader 类加载器<br>
 interfaces 实现接口<br>
 <p>
 JDK动态代理的一般实现步骤如下：<br>
（1）创建一个实现InvocationHandler接口的类，它必须实现invoke方法<br>
（2）创建被代理的类以及接口<br>
（3）调用Proxy的静态方法newProxyInstance，创建一个代理类<br>
（4）通过代理调用方法<br>
 
#### 二 . Hook机制之Binder Hook
详见[Android插件化原理解析——Hook机制之Binder Hook](http://weishu.me/2016/02/16/understand-plugin-framework-binder-hook/)<br>
下面这些话都是摘抄的原作者的。<br>
android系统通过Binder机制给应用程序提供了一系列的系统服务，比如ActivityManagerService,ClipboardManager,AudioManager等；这些广泛存在的系统服务
给应用程序提供了诸如任务管理、音频、视频等异常强大的功能。<br>
通过分析我们得知，系统Service的使用其实就分为两步:<br>
``` java
1 IBinder b = ServiceManager.getService("service_name"); // 获取原始的IBinder对象
2 IXXInterface in = IXXInterface.Stub.asInterface(b); // 转换为Service接口
```
总结一下，要达到修改系统服务的目的，我们需要如下两步：<br>
 1.首先肯定需要伪造一个系统服务对象，接下来就要想办法让asInterface能够返回我们的这个伪造对象而不是原始的系统服务对象。<br>
 2.通过上文分析我们知道，只要让getService返回IBinder对象的queryLocalInterface方法直接返回我们伪造过的系统服务对象就能达到目的。所以，我们需要伪造    一个IBinder对象，主要是修改它的queryLocalInterface方法，让它返回我们伪造的系统服务对象；然后把这个伪造对象放置在ServiceManager的缓存map里面即    可。<br>
然后文章给我们演示了如何Hook系统的剪贴板服务。<br>
插件框架当然不会做替换文本这么无聊的事情，DroidPlugin插件框架管理插件使得插件就像是主程序一样，因此插件需要使用主程序的剪切版，插件之间也会共用剪切版；其他的一些系统服务也类似，这样就可以达到插件和宿主程序之间的天衣服缝，水乳交融。 

#### 三 . Hook机制之ams&pms
详见[Android 插件化原理解析——Hook机制之AMS&PMS](http://weishu.me/2016/03/07/understand-plugin-framework-ams-pms-hook/)<br>
Hook并不是一项神秘的技术；一个干净，透明的框架少不了AOP，而AOP也少不了Hook.<br>
作者所讲解的Hook仅仅使用反射和动态代理技术，更加强大的Hook机制可以进行字节码编织，比如J2EE广泛使用了cglib和asm进行AOP编程；而Android上现有的插件框架还是加载编译时代码，采用动态生成类的技术理论上也是可行的。<br>
AOP（Aspect Oriented Programming）编程，即面向切面编程。<br>

#### 四 . Activity生命周期管理
在Java平台上要做到动态运行模块、热插拔可以使用ClassLoader技术进行动态类加载，比如广泛使用的OSGI技术。在Android上当然也可以使用动态加载技术，但是仅仅完成动态类加载是不够的，我们需要想办法把我们加载进来的Activity等组件交给系统管理，让AMS赋予组件声明周期。这样才算是一个有血有肉的完善的插件化方案。<br>
##### AndroidManifest.xml的限制
在Android中有一个限制：必须在AndroidManifest.xml中显式声明使用的Activity。如果不声明，那么在程序运行的时候，一般会遇到如下异常：
``` java
06-21 09:19:13.268 4462-4462/com.liuh.intercept_activity E/AndroidRuntime: FATAL EXCEPTION: main
                                                                           Process: com.liuh.intercept_activity, PID: 4462
                                                                           android.content.ActivityNotFoundException: Unable to find explicit activity class {com.liuh.intercept_activity/com.liuh.intercept_activity.TargetActivity}; have you declared this activity in your AndroidManifest.xml?
```
这个限制在很大程度上限制了插件系统的发挥：假设我们需要启动一个插件的Activity，插件使用的Activity是无法预知的，这样肯定不会在AndroidManifest.xml文件中声明；如果插件中新添加了一个Activity，主程序的AndroidManifest.xml就需要更新；既然双方都需要升级，何必使用插件呢？这已经违背了动态加载的初衷：不修改插件框架而动态扩展功能。
##### 如何绕过“必须在AndroidManifest.xml中显式声明使用的Activity”的限制
App进程与AMS进程的通讯过程如下图：
![App和AMS交互流程](https://github.com/liuhuan2015/Learn-understand-plugin-framework/blob/master/images/App_with_AMS.png)<br>

 1 . App进程会委托AMS进程完成Activity生命周期的管理以及任务栈的管理；这个通信过程中AMS是Server端，App进程通过持有AMS的client代理ActivityManagerNative完成通信过程。<br>
 2 . AMS进程完成生命周期管理以及任务栈管理后，会把控制权交给App进程，让App进程完成Activity类对象的创建，以及生命周期回调；这个通信过程也是通过Binder完成的，App所在server端的Binder对象存在于ActivityThread的内部类ApplicationThread;AMS所在client通过持有IApplicationThread的代理对象完成和App进程的通信。<br>
 
Activity的启动过程用一张图简单描述如下：
![Activity简要启动流程](https://github.com/liuhuan2015/Learn-understand-plugin-framework/blob/master/images/Activity_launch.png)<br>

先从App进程调用startActivity；然后通过IPC调用进入系统进程system_server，完成Activity管理以及一些校验工作；最后又回到了APP进程完成真正的Activity对象创建。<br>

为了绕过“必须在AndroidManifest.xml中显式声明使用的Activity”的限制，可以在第一步假装启动一个已经在AndroidManifest.xml里面声明过的替身Activity，让这个Activity进入AMS进程接受校验；最后在第三步的时候换成我们真正要启动的Activity；这样就成功的欺骗了AMS进程。<br>

具体实现见原文和工程代码（使用的是Hook技术）。<br>

在实际的插件系统中，要启动的目标Activity肯定存在于一个单独的文件中，系统默认的ClassLoader无法加载插件中的Activity类————系统压根儿就不知道要加载的插件在哪里，谈何加载。这是一个很重要的问题需要处理。<br>

我们要完成插件系统中类的加载，可以通过自定义ClassLoader来实现。解决了“启动没有在AndroidManifest中显式声明的，并且存在于外部文件中的Activity”的问题，插件系统对于Activity的管理才算得上是一个完全体。（这个见后续文章）<br>

#### 五 . Android插件加载机制
通常情况下，插件均是以独立的文件存在甚至是通过网络获取的，这时我们怎么才能从宿主程序中启动插件中的Activity呢？<br>
问题：如何启动没有在AndroidManifest.xml中显式声明，并且存在于外部插件中的Activity。<br>

##### ClassLoader机制
Java虚拟机把描述类的数据从Class文件加载到内存，并对数据进行校验、转换解析和初始化，最终形成可以被虚拟机直接使用的Java类型，这就是虚拟机的类加载机制。<br>

Java的类加载机制是一个相对复杂的过程；它包括加载、验证、准备、解析、初始化五个阶段；对于开发者来说，可控性最强的是加载阶段；加载阶段主要完成三件事情：<br>
1.根据一个类的全限定名来获取定义此类的二进制字节流<br>
2.将这个字节流所代表的静态存储结构转化为JVM方法区中的运行时数据结构<br>
3.在内存中生成一个代表这个类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口<br>

『通过一个类的全限定名获取描述此类的二进制字节流』这个过程被抽象出来，就是Java的类加载器模块，也即JDK中ClassLoader API<br>

Android Framework提供了DexClassLoader这个类，简化了『通过一个类的全限定名获取描述次类的二进制字节流』这个过程；我们只需要告诉DexClassLoader一个dex文件或者apk文件的路径就能完成类的加载。因此本文的内容用一句话就可以概括：<br>

**将插件的dex或者apk文件告诉『合适的』DexClassLoader，借助它完成插件类的加载**

##### 激进方案：Hook掉系统的ClassLoader，自己操刀
最终目的是调用getPackageInfoNoCheck得到LoadedApk的信息，并替换其中的mClassLoader然后把其添加到ActivityThread的mPackages缓存中；从而达到我们使用自己的ClassLoader加载插件中的类的目的<br>
LoadedApk对象是APK文件在内存中的表示。 Apk文件的相关信息，诸如Apk文件的代码和资源，甚至代码里面的Activity，Service等组件的信息我们都可以通过此对象获取<br>
在这种方案中，全盘接管了插件中类的加载过程，使用了一个自定义的ClassLoader，是一种相对暴力的解决方案。

##### 保守方案：委托系统，让系统帮忙加载
原理：<br>
1 . 默认情况下performLacunchActivity会使用替身StubActivity的ApplicationInfo也就是宿主程序的CLassLoader加载所有的类；我们的思路是告诉宿主ClassLoader我们在哪，让其帮助完成类加载的过程。<br>
2 . 宿主程序的ClassLoader最终继承自BaseDexClassLoader，BaseDexClassLoader通过DexPathList进行类的查找过程；而这个查找通过遍历一个dexElements的数组完成；我们通过把插件dex添加进这个数组就让宿主ClasLoader获取了加载插件类的能力。<br>

##### 两种方案的总结
1 . 『激进方案』中我们自定义了插件的ClassLoader，并且绕开了Framework的检测；利用ActivityThread对于LoadedApk的缓存机制，我们把携带这个自定义的ClassLoader的插件信息添加进mPackages中，进而完成了类的加载过程<br>
2 . 『保守方案』中我们深入探究了系统使用ClassLoader findClass的过程，发现应用程序使用的非系统类都是通过同一个PathClassLoader加载的；而这个类的最终父类BaseDexClassLoader通过DexPathList完成类的查找过程；我们hack了这个查找过程，从而完成了插件类的加载<br>

**『激进方案』是多ClassLoader构架,『保守方案』是单ClassLoader方案**

在J2EE领域中广泛使用ClasLoader的地方均采用多ClassLoader架构，比如Tomcat服务器，Java模块化事实标准的OSGi技术；所以，我们有足够的理由认为选择多ClassLoader架构在大多数情况下是明智之举<br>

目前开源的插件方案中，DroidPlugin采用的『激进方案』，Small采用的『保守方案』<br>















 
