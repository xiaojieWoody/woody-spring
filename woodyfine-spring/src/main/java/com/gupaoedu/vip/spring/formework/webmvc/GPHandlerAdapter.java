package com.gupaoedu.vip.spring.formework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

//专人干专事
public class GPHandlerAdapter {

    private Map<String, Integer> paramMapping;

    public GPHandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }


    //handler中包含了controller、method、url信息
    //根据请求参数信息，跟method中的参数信息进行动态匹配
    //response传进来的目的只是为了将其赋值给方法参数
    //只有当用户传过来的ModelAndView为空的时候，才会new一个默认的
    public GPModelAndView handle(HttpServletRequest request, HttpServletResponse response, GPHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //1. 准备方法的形参列表
        //方法重载：形参的决定因素：参数的个数、参数的类型、参数的位置、方法名称
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

        //2.拿到自定义命名参数所在位置
        //用户通过URL传过来的参数列表
        Map<String, String[]> reqParameterMap = request.getParameterMap();

        //3. 构造实参列表
        Object[] paramValues = new Object[parameterTypes.length];
        for(Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            if(!this.paramMapping.containsKey(param.getKey())){continue;}
            int index = this.paramMapping.get(param.getKey());
            //因为页面上传过来的值都是String类型的，而在方法中定义的类型是千变万化的
            //要针对传过来的参数进行类型转换
            paramValues[index] = caseStringValue(value, parameterTypes[index]);
        }
        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;
        }
        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;
        }

        //4. 从handler中取出controller、method，然后利用反射机制进行调用
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if(result == null) {
            return null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == GPModelAndView.class;
        if(isModelAndView) {
            return (GPModelAndView)result;
        }else {
            return null;
        }
    }

    public Object caseStringValue(String value, Class<?> clazz) {
        if(clazz == String.class) {
            return value;
        } else if(clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if(clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }
}
