package xyz.ytora.toolkit.document.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by yangtong on 2025/4/4 下午4:46
 * <br/>
 * Excel注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Excel {

    /**
     * excel文件名称/excel列名称
     */
    String value() default "";

    /**
     * 字段顺序
     */
    int index() default Integer.MAX_VALUE;

    /**
     * 列宽度
     */
    int width() default 20;

    /**
     * 日期、数字格式
     */
    String format() default "";

    /**
     * 是否跳过该字段
     */
    boolean ignore() default false;

    /**
     * 表头所在行,默认索引为0的行（也就是第一行）为表头行
     */
    int headerRowIndex() default 0;

    /**
     * 从指定开始读取，默认从索引为1的行（也就是第二行）开始读取数据
     */
    int startRow() default 1;
}