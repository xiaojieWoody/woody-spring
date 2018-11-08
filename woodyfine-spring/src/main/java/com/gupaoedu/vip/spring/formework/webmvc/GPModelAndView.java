package com.gupaoedu.vip.spring.formework.webmvc;

import java.util.Map;

public class GPModelAndView {

    private String viewName;
    private Map<String, ?> model;

    public GPModelAndView(String viewName, Map<String, ?>model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
