package com.liuh.dynamic_proxy_hook;

/**
 * Date: 2018/5/22 08:54
 * Description:对购物接口最原始的实现，可以理解为亲自去商店购物
 */

public class ShoppingImpl implements IShopping {
    @Override
    public Object[] doShopping(long money) {
        System.out.println("逛淘宝，逛商场，买买买！！");
        System.out.println(String.format("花了%s块钱", money));
        return new Object[]{"鞋子", "衣服", "零食"};
    }
}
