package com.liuh.dynamic_proxy_hook.staticproxy;

import com.liuh.dynamic_proxy_hook.IShopping;

import java.util.Arrays;

/**
 * Date: 2018/5/22 08:58
 * Description:静态代理，可以理解为我们没有时间自己亲自去买东西，于是我们找了一个代购帮我们去买.
 * 我们把要买什么东西给他说一下（即接口里面的功能方法），把钱给他（即接口里面的功能方法的参数）。
 */

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
