package com.lightormdatabase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * package kotlinTest:com.qing.lightormdatabase.lightorm.LightTableName.class
 * 作者：zyq on 2017/8/23 17:17
 * 邮箱：zyq@posun.com
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LightTableName {
    String value() default "";
}
