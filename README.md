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
