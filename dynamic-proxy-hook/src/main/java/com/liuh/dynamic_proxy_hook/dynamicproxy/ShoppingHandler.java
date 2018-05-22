package com.liuh.dynamic_proxy_hook.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Date: 2018/5/22 10:09
 * Description:
 * Java动态代理位于java.lang.reflect包下，一般主要涉及到以下两个类：
 * （1）Interface InvocationHandler
 * 该接口中仅定义了一个方法：public Object invoke(Object obj, Method method, Object[] args)，
 * 在使用时，第一个参数obj一般是指被代理的对象，method是被代理的方法，args为该方法的参数数组。
 * 这个抽象方法在代理类中动态实现。
 * <p>
 * （2）Proxy
 * <p>
 * 该类即为动态代理类，
 * static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)，
 * 返回代理类的一个实例，返回后的代理类可以当作被代理类使用
 * loader 类加载器
 * interfaces 实现接口
 * <p>
 * JDK动态代理的一般实现步骤如下：
 * （1）创建一个实现InvocationHandler接口的类，它必须实现invoke方法
 * （2）创建被代理的类以及接口
 * （3）调用Proxy的静态方法newProxyInstance，创建一个代理类
 * （4）通过代理调用方法
 */

public class ShoppingHandler implements InvocationHandler {

    //被代理的原始对象
    Object base;

    public ShoppingHandler(Object base) {
        this.base = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("doShopping".equals(method.getName())) {
            //如果方法名称是doShopping
            long money = (long) args[0];
            long costMoney = (long) (money * 0.5);

            System.out.println(String.format("顾客支付了%s块钱",money));
            System.out.println(String.format("代理实际花了%s块钱", costMoney));

            Object[] things = (Object[]) method.invoke(base, costMoney);

            if (things != null && things.length > 0) {
                things[0] = "被掉包的东西！";
            }
            return things;
        }

        if ("doSomethingElse".equals(method.getName())) {
            //可以理解为代理别的，做些别的事情
            return null;
        }

        return null;
    }
}
