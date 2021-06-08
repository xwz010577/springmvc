package com.wwq.meetings.framwork.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在传递给controller之前进行解密
 * @author wwq
 * @date 2021/5/20-10:38
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeAdvisor {
    boolean value() default false;
}
