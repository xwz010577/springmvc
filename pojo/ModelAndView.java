package com.wwq.meetings.framwork.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * 包含返回的数据以及返回的路径
 * @author wwq
 * @date 2021/5/20-9:22
 */
public class ModelAndView implements AbstractModelAndView {
    /**
     * 存放需要跳转的视图
     */
    private String viewName;
    /**
     * 每一个线程都有这个临时容器，存入返回数据、路径
     */
    private Map<String,Object> attribute = new HashMap<>();

    @Override
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public void addAttribute(String key,Object value){
        attribute.put(key,value);
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getAttribute() {
        return attribute;
    }
}
