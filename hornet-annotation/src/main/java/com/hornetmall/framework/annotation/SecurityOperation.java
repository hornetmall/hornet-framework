package com.hornetmall.framework.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface SecurityOperation {

    String name() default "";

    String displayName() default "";

    String[] references() default {};
}
