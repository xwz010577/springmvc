package com.wwq.meetings.framwork.result;

/**
 * 错误信息枚举类
 * @author wwq
 * @date 2021/5/18-9:48
 */
public enum ResponseCode implements ResponsecodeInterce{
    /**
     * 响应错误信息
     */
    CONFIG_ERROR(1,"配置文件的配置信息有误不能转换Java的class对象"),
    CONFIG_NO_ERROR(2,"没有注解配置"),
    CONFIG_NO_SCANNERPATH_ERROR(3,"配置文件没有填写路径"),
    CONFIG_ANNOATION_ERROR(4,"方法上的mapping必须填写地址"),
    CONFIG_METHOD_ERROR(5,"请求方法错误，应该是post"),
    CONFIG_JSON_ERROR(6,"JSON格式转换错误"),
    REQUEST_PATH_ERROR(7,"请求地址错误"),
    EXCEPTION_NO_ERROR(8,"没有异常处理注解，请添加！！！"),
    CLASS_TO_ERROR(9,"注解类型转换对象异常"),
    STAFF_ID_ERROR(10,"注解类型转换对象异常")


    ;

    /**
     * 状态码
     */
    private int code;
    /**
     * 状态响应信息
     */
    private String message;
    ResponseCode(int code, String message) {
        this.code=code;
        this.message=message;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public void setCode(int code) {

    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public void setMessage(String message) {

    }
}
