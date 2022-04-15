package com.gitee.xiezengcheng.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiezengcheng
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    //  基本对象的属性名
    String propName() default "";

    // 查询方式
    Type type() default Type.EQUAL;

    /**
     * 连接查询的属性名，如User类中的dept
     */
    String joinName() default "";

    /**
     * 默认左连接
     */
    Join join() default Join.LEFT;

    /**
     * 多字段模糊搜索，仅支持String类型字段， 如@Query(blurry = {"email","username"})
     */
    String[] blurry() default {};

    enum Type {
        // 相等
        EQUAL
        // 大于
        , GREATER_THAN
        // 大于等于
        , GREATER_THAN_EQ
        //  小于
        , LESS_THAN
        // 小于等于
        , LESS_THAN_EQ
        // 中模糊查询
        , INNER_LIKE
        //  左模糊查询
        , LEFT_LIKE
        //  右模糊查询
        , RIGHT_LIKE
        // 包含
        , IN
        // 不包含
        , NOT_IN
        // 不等于
        ,NOT_EQUAL
        // Between
        ,BETWEEN
        // 不为空
        ,NOT_NULL
        // 为空
        ,IS_NULL
        ,TRUE
        ,FALSE
        ,NOT_LIKE
        ,BLURRY
    }

    /**
     * 适用于简单连接查询，复杂的请自定义该注解，或者使用sql查询
     */
    enum Join {
        LEFT, RIGHT, INNER
    }

}

