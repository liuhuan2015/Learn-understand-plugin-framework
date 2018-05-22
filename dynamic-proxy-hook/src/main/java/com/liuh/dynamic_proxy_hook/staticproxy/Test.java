package com.liuh.dynamic_proxy_hook.staticproxy;

import com.liuh.dynamic_proxy_hook.ShoppingImpl;

import java.util.Arrays;

/**
 * Date: 2018/5/22 09:07
 * Description:静态代理的测试类
 */

public class Test {

    public static void main(String[] args) {
        StaticProxyShopping staticProxyShopping = new StaticProxyShopping(new ShoppingImpl());
        Object[] objs = staticProxyShopping.doShopping(100);
        System.out.println("我交给顾客的东西：" + Arrays.toString(objs));
    }
}
