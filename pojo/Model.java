package com.wwq.meetings.framwork.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wwq
 * @date 2021/5/19-15:34
 */
public class Model {
    /**
     * 临时容器，存放需要传递的数据
     */
    private Map<String,Object> attributes = new HashMap<String,Object>();

    public void addAttribute(String key,Object value){
        attributes.put(key,value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
