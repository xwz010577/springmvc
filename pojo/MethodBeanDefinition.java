package com.wwq.meetings.framwork.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author wwq
 * @date 2021/5/17-15:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodBeanDefinition<T> {
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 方法请求路径
     */
    private String requestMapping;
    /**
     * 方法的返回参数类型
     */
    private T returnType;
    /**
     * 方法的注解类型
     */
    private Class annotationClass;
    /**
     * 当前方法
     */
    private Method method;
    /**
     * 是否有requsetBody
     */
    private boolean isResponseBodyOrNot;
    /**
     * 加密、解密
     */
    private boolean beforeAdvisorOrNot;
    private boolean afterAdvisorOrNot;

    /**
     * 参数集合
     */
    private List<ParameterBeanDefinition> methodParameters;

}
