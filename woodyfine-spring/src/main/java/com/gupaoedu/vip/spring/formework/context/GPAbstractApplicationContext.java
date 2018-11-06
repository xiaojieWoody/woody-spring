package com.gupaoedu.vip.spring.formework.context;

public abstract class GPAbstractApplicationContext {

    //提供给子类重写
    protected void onRefresh() {

    }

    protected abstract void refreshBeanFactory();
}
