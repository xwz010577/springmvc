package com.wwq.meetings.framwork.exception;

/**
 * @author wwq
 * @date 2021/5/18-9:41
 */
public class FramworkException extends RuntimeException {
    private int code;
    private String message;

    public FramworkException(int code,String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
