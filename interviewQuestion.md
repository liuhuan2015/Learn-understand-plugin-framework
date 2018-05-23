#面试题
####1.静态代理与动态代理区别是什么，分别用在什么样的场景里？
静态代理与动态代理的区别在于代理类生成的时间不同，如果需要对多个类进行代理，并且代理的功能都是一样的，用静态代理重复编写代理类就非常的麻烦，可以用动态代理动态的生成代理类。
''' java
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
'''
