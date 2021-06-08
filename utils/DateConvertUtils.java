package com.wwq.meetings.framwork.utils;

import org.apache.commons.beanutils.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wwq
 * @date 2021/6/3-23:37
 * @description：时间转换器
 */
public class DateConvertUtils implements Converter {
    @Override
    public <T> T convert(Class<T> aClass, Object o) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String str = (String) o;
        Date date = null;
        try {
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (T) date;
    }
}
