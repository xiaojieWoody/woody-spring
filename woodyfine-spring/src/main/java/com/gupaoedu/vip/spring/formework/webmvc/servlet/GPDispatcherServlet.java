package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import com.gupaoedu.vip.spring.formework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

//servlet只是作为一个MVC的入口
public class GPDispatcherServlet extends HttpServlet {


    private  final String LOCATION = "contextConfigLocation";

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化IOC容器
        GPApplicationContext context = new GPApplicationContext(config.getInitParameter(LOCATION));
    }
}
