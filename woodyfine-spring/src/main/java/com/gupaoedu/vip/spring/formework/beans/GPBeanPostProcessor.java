package com.gupaoedu.vip.spring.formework.beans;

//事件监听
public class GPBeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }
}
