package com.liuh.dynamic_proxy_hook;

/**
 * Date: 2018/5/22 08:52
 * Description:购物的接口，只有一个功能：购物，接收参数是long money.
 */

public interface IShopping {

    Object[] doShopping(long money);

}
