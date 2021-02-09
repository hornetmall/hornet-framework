package com.hornetmall.framework.annotation;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SecurityResource {

    public enum Type {
        Table
    }

    Type type() default Type.Table;

    String name();

    String[] columns();
}
