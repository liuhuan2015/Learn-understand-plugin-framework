package com.liuh.dynamic_proxy_hook.dynamicproxy;

import com.liuh.dynamic_proxy_hook.IShopping;
import com.liuh.dynamic_proxy_hook.ShoppingImpl;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Date: 2018/5/22 10:23
 * Description:动态代理的测试类
 */

public class Test {

    public static void main(String[] args) {
        IShopping women = new ShoppingImpl();

        //正常购物
        System.out.println("-----------自己亲自去购物-----开始---------");
        System.out.println("我买到了：" + Arrays.toString(women.doShopping(100)));
        System.out.println("-----------自己亲自去购物-----结束---------");

        System.out.println("---------------------------华丽分割线----------------------------------------------------");

        //使用动态代理去购物
        System.out.println("-----------使用动态代理去购物-----开始---------");
        //TODO 下面的写法感觉有点看不懂，传进去的参数都和women有关，最终把返回值又赋给了women
        //动态代理，动态实现，多态
        women = (IShopping) Proxy.newProxyInstance(women.getClass().getClassLoader(),
                women.getClass().getInterfaces(), new ShoppingHandler(women));
        System.out.println("代理帮我买到了：" + Arrays.toString(women.doShopping(100)));
        System.out.println("-----------使用动态代理去购物-----结束---------");
    }
}
