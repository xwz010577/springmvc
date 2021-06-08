package com.wwq.meetings.framwork.pojo;

/**
 * @author wwq
 * @date 2021/5/20-9:29
 */
public interface AbstractModelAndView {
    /**
     * 设置返回数据
     * @param viewName
     */

    public void setViewName(String viewName);

    /**
     * 返回路径
     * @param key
     * @param value
     */
    public void addAttribute(String key,Object value);
}
