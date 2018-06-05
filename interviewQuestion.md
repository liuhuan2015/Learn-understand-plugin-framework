#面试题
#### 1.静态代理与动态代理区别是什么，分别用在什么样的场景里？
静态代理与动态代理的区别在于代理类生成的时间不同，如果需要对多个类进行代理，并且代理的功能都是一样的，用静态代理重复编写代理类就非常的麻烦，可以用动态代理动态的生成代理类。
```java
// 为目标对象生成代理对象
public Object getProxyInstance() {
    return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("开启事务");

                    // 执行目标对象方法
                    Object returnValue = method.invoke(target, args);

                    System.out.println("提交事务");
                    return null;
                }
            });
}
```
代理可以理解为我们没有时间亲自去买东西，于是我们找了一个代购帮我们去买。<br>
传统的静态代理模式需要为每一个被代理的类写一个代理类，即顾客和代理商是一对一的关系。在这种情况下，如果需要被代理的类有很多时，使用静态代理就不合适了。<br>
动态代理模式可以简单理解为JVM可以在运行时帮我们动态生成一系列的代理类,这样我们就不需要手动写每一个静态的代理类了.动态代理主要涉及到两个类：<br>
1、Proxy <br>
该类即为动态代理类，<br>
static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)，<br>
返回代理类的一个实例，返回后的代理类可以当作被代理类使用<br>
loader 类加载器<br>
interfaces 实现接口<br>
2、java.lang.reflect.InvocationHandler<br>
该接口中仅定义了一个方法：public Object invoke(Object obj, Method method, Object[] args)，<br>
在使用时，第一个参数obj一般是指被代理的对象，method是被代理的方法，args为该方法的参数数组。<br>
这个抽象方法在代理类中动态实现。<br>

JDK动态代理的一般实现步骤如下：<br>
（1）创建一个实现InvocationHandler接口的类，它必须实现invoke方法<br>
（2）创建被代理的类以及接口<br>
（3）调用Proxy的静态方法newProxyInstance，创建一个代理类<br>
（4）通过代理调用方法<br>
具体使用可以见[Learn-understand-plugin-framework](https://github.com/liuhuan2015/Learn-understand-plugin-framework)

#### 2.ActivityManager，ActivityManagerService以及ActivityManagerNative之间的关系
翻阅系统的ActivityManagerServer的源码，我们就会知道哪一个类是什么角色了：<br>
IActivityManager是一个IInterface，它代表远程Service具有什么能力，<br>
ActivityManagerNative指的是Binder本地对象（类似AIDL工具生成的Stub类），这个类是抽象类，它的实现是ActivityManagerService；因此对于AMS的最终操作都会进入ActivityManagerService这个真正实现；<br>
同时如果仔细观察，ActivityManagerNative.java里面有一个非公开类ActivityManagerProxy, 它代表的就是Binder代理对象；这个和AIDL模型是一模一样的。<br>
ActivityManager呢？它是一个管理类，真正的操作都是转发给ActivityManagerNative进而交给他的实现ActivityManagerService 完成的。<br>
ActivityManagerNative实际上就是ActivityManagerService这个远程对象的Binder代理对象；每次需要与AMS打交道的时候，需要借助这个代理对象通过Binder驱动进而完成IPC调用<br>
由于整个Framework与AMS打交道是如此频繁，framework使用了一个单例把这个AMS的代理对象保存了起来；这样只要需要与AMS进行IPC调用，获取这个单例即可。这是AMS这个系统服务与其他普通服务的不同之处<br>
详见[Binder学习指南](http://weishu.me/2016/01/12/binder-index-for-newer/)

