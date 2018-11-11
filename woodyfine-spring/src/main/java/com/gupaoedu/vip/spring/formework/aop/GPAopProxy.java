package com.gupaoedu.vip.spring.formework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//动态代理
public class GPAopProxy implements InvocationHandler {

    private GPAopConfig config;
    private Object target;

    //把原生的对象传进来
    public Object getProxy(Object instance) {
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        //作业：利用AOP思想，自己去实现一个TransactionManager
        //需要补充：把Method的异常拿到，把Method的方法拿到
        //把Method的参数拿到
        //args的值就是实参


        //原始方法调用以前要执行增强的代码
        if(config.contains(m)) {
            GPAopConfig.GPAspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        //反射调用原始方法
        Object obj = method.invoke(this.target, args);
        System.out.println(args);

        //原始方法调用以后要执行增强的代码
        if(config.contains(m)) {
            GPAopConfig.GPAspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }


        //将最原始的返回值返回出去
        return obj;
    }

    public void setConfig(GPAopConfig config) {
        this.config = config;
    }
}
