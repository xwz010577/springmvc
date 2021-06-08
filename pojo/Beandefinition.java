package com.wwq.meetings.framwork.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wwq
 * @date 2021/5/17-15:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Beandefinition {
    /**
     * 类的名称
     */
    private String typeName;
    /**
     * 类的class文件
     */
    private Class clazz;
    /**
     * 表示这个类使用了那些注解
     */
    private Class[] annotationNames;
    /**
     * controller类上面requestMapping的值
     */
    private String requestMapping = "";
    /**
     * 方法
     */
    private MethodBeanDefinition methodBeanDefinition;
}
