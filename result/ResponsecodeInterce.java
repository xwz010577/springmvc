package com.wwq.meetings.framwork.result;

/**
 * @author wwq
 * @date 2021/5/18-9:44
 */
public interface ResponsecodeInterce {
    /**
     * 获取响应吗
     * @return
     */
    int getCode();

    /**
     * 设置响应码
     * @param code
     */
    void setCode(int code);

    /**
     * 获取响应值
     * @return
     */
    String getMessage();

    /**
     * 设置响应信息
     * @param message
     */
    void setMessage(String message);
}
