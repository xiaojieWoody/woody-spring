package com.gupaoedu.vip.spring.formework.context;

import com.gupaoedu.vip.spring.demo.action.MyAction;
import com.gupaoedu.vip.spring.formework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPService;
import com.gupaoedu.vip.spring.formework.beans.GPBeanDefinition;
import com.gupaoedu.vip.spring.formework.beans.GPBeanPostProcessor;
import com.gupaoedu.vip.spring.formework.beans.GPBeanWrapper;
import com.gupaoedu.vip.spring.formework.context.support.GPBeanDefinitionReader;
import com.gupaoedu.vip.spring.formework.core.GPBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String[] configLocations;

    //保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new HashMap<String, Object>();

    //存储所有被代理过的对象
    private Map<String, GPBeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, GPBeanWrapper>();

    private GPBeanDefinitionReader reader;

    public GPApplicationContext(String ... configLocations) {
        this.configLocations = configLocations;

        refresh();
    }

    private void refresh() {

        //定位
        this.reader = new GPBeanDefinitionReader((configLocations));

        //加载
        List<String> beanDefintions = reader.loadBeanDefinitions();

        //注册
        doRegistry(beanDefintions);

        //依赖注入（lazy-init = false） 要是执行依赖注入，这里是自动调用getBean方法
        doAutowired();

        //断点测试依赖是否注入
        MyAction myAction = (MyAction)this.getBean("myAction");
        //myAction.query(null,null,"任性的Tom老师");
    }

    //自动化依赖注入
    private void doAutowired() {
        for(Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()) {
                Object obj = getBean(beanName);
            }
        }

        //被注入依赖的类实例化
        //循环依赖或递归依赖
        //两次循环有点坑

        //暂时
        for(Map.Entry<String, GPBeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()) {
            populateBean(beanWrapperEntry.getKey(), beanWrapperEntry.getValue().getOriginalInstance());
        }
    }

    //将BeanDefinℹitions注册到beanDefinitionMap中
    private void doRegistry(List<String> beanDefintions) {
        //beanName 有三种情况：1、默认是类名首字母小写 2、自定义名字 3、接口注入
        try {
            for(String className : beanDefintions) {
                Class<?> beanClass = Class.forName(className);

                //接口不能实例化
                if(beanClass.isInterface()) {
                    continue;
                }

                GPBeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition );
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for(Class<?> i : interfaces) {
                    //如果是多个实现类，只能覆盖，也可以自定义名字
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);
                }
                //容器初始化完毕
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 依赖注入
     * 通过读取BeanDefinition中的信息，然后通过反射机制创建一个实例并返回
     * Spring的做法：不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
     * 装饰器模式"
     * 1、保留原来的OOP关系
     * 2、需要对它进行扩展，增强（为以后AOP打基础）
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        String className = beanDefinition.getBeanClassName();

        try {
             //通知事件
            GPBeanPostProcessor beanPostProcessor = new GPBeanPostProcessor();

            //传入一个BeanDefinition，返回一个实例
            Object instance = instantionBean(beanDefinition);
            if(null == instance) {
                return null;
            }

            //实例初始化之前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

            GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName, beanWrapper);

            //实例初始化之后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            //返回包装类，留有操作空间
            return this.beanWrapperMap.get(beanName).getWrapperInstance();

        } catch (Exception e) {

        }


        return null;
    }

    private Object instantionBean(GPBeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            //根据Class才能确定一个类是否有实例
            if(this.beanCacheMap.containsKey(className)) {
                instance = this.beanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void populateBean(String beanName, Object instance) {
        Class<?> clazz = instance.getClass();
        if(!(clazz.isAnnotationPresent(GPController.class)) ||
                clazz.isAnnotationPresent(GPService.class)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            if(!field.isAnnotationPresent(GPAutowired.class)){
                continue;
            }

            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
