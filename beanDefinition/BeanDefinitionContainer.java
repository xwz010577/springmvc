package com.wwq.meetings.framwork.beanDefinition;

import com.wwq.meetings.framwork.pojo.Beandefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wwq
 * @date 2021/5/17-15:45
 */
public class BeanDefinitionContainer {
    /**
     * 存放请求路径以及类、方法的描述
     */
    private static final Map<String, Beandefinition> requestPathAndBeanDefinitionContainer = new ConcurrentHashMap<>();

    public static Map<String, Beandefinition> getRequestPathAndBeanDefinitionContainer() {
        return requestPathAndBeanDefinitionContainer;
    }

}
