package com.gupaoedu.vip.spring.formework.context.support;

import com.gupaoedu.vip.spring.formework.beans.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 用对配置文件进行查找，读取、解析
 */
public class GPBeanDefinitionReader {

    private Properties config = new Properties();
    //在配置文件中，用来获取自动扫描的包名的key
    private final String SCAN_PACKAGE = "scanPackage";

    private  List<String> registryBeanClasses = new ArrayList<String>();

    public GPBeanDefinitionReader(String ... locations) {

        //Spring中通过Reader去查找和定位
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        try {
            config.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //扫描
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    //每注册一个className，就返回一个BeanDefinition，自己包装
    //只是为了对配置信息进行一个包装
    public GPBeanDefinition registerBean(String className) {
        if(this.registryBeanClasses.contains(className)) {
            GPBeanDefinition beanDefinition = new GPBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }
        return null;
    }

    public List<String> loadBeanDefinitions() {
        return this.registryBeanClasses;
    }

    public Properties getConfig() {
        return this.config;
    }

    //扫描Class，并保存到List中
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                registryBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
