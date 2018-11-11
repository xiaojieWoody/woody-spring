package com.gupaoedu.vip.spring.formework.beans;

import com.gupaoedu.vip.spring.formework.aop.GPAopConfig;
import com.gupaoedu.vip.spring.formework.aop.GPAopProxy;
import com.gupaoedu.vip.spring.formework.core.GPFactoryBean;

public class GPBeanWrapper extends GPFactoryBean {

    private GPAopProxy aopProxy = new GPAopProxy();

    //会用到观察者模式
    //支持事件响应，会有一个监听
    private GPBeanPostProcessor postProcessor;

    private Object wrapperInstance;
    //反射生成的原始类包装一下
    private Object originalInstance;

    public GPBeanWrapper(Object instance) {
//        this.wrapperInstance = instance;
        this.wrapperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public void setAopConfig(GPAopConfig config) {
        aopProxy.setConfig(config);
    }

    public Object getWrapperInstance() {
        return this.wrapperInstance;
    }

    public Object getOriginalInstance() {
        return this.originalInstance;
    }

    //返回代理后的Class，可能会是这个$Proxy0
    public Class<?> getWrapperClass() {
        return this.wrapperInstance.getClass();
    }

    public GPBeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(GPBeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }
}
