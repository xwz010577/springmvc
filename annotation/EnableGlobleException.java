package com.wwq.meetings.framwork.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wwq
 * @date 2021/5/27-9:55
 * @description：是否处理异常
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableGlobleException {
    String exceptionPath() default "";
}
