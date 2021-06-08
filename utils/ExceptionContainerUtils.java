package com.wwq.meetings.framwork.utils;

import com.wwq.meetings.framwork.exception.GobleFramwordExcepetion;

/**
 * @author wwq
 * @date 2021/5/27-10:40
 * @description：
 */
public class ExceptionContainerUtils {

    private static GobleFramwordExcepetion gobleFramwordExcepetion;

    public static GobleFramwordExcepetion getGobleFramwordExcepetion() {
        return gobleFramwordExcepetion;
    }

    public static void setGobleFramwordExcepetion(GobleFramwordExcepetion gobleFramwordExcepetion) {
        ExceptionContainerUtils.gobleFramwordExcepetion = gobleFramwordExcepetion;
    }
}
