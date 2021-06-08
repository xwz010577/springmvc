package com.wwq.meetings.framwork.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wwq
 * @date 2021/5/17-15:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterBeanDefinition<T> {
    /**
     * 参数名称
     */
    private String parameterName;
    /**
     * 参数类型
     */
    private T parameterType;
    /**
     * 参数在列表中的位置
     */
    private int position;
    /**
     * 是否被requestbody注解
     */
    private boolean isRequestBodyOrNot;
}
