package com.gupaoedu.vip.spring.formework.context;

import com.gupaoedu.vip.spring.formework.beans.GPBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {

    //用来保存配置信息
    protected Map<String, GPBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, GPBeanDefinition>();

    @Override
    protected void onRefresh(){

    }

    @Override
    protected void refreshBeanFactory() {

    }
}
