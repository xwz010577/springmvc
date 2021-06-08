package com.wwq.meetings.framwork.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wwq
 * @date 2021/5/27-9:48
 * @description：
 */
public abstract class GobleFramwordExcepetion {
    /**
     * 处理异常信息
     * @param err
     * @param req
     * @param resp
     */
    protected abstract void handleException(Exception err, HttpServletRequest req, HttpServletResponse resp);

    /**
     * 预处理异常，交给回调去执行（即子类执行）
     * @param err
     * @param req
     * @param resp
     */
    public void prehandleException(Exception err, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //调用上面的方法，暴露给开发者
        handleException(err,req,resp);

    }
}
