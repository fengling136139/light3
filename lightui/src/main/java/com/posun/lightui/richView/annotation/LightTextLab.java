package com.posun.lightui.richView.annotation;

import android.text.InputType;

import com.posun.lightui.richView.LightRichType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LightTextLab {
    String lab() default "lable";

    String value() default "";

    int type() default LightRichType.TEXT_NORMAL;//使用类型 android.text.InputType
}
