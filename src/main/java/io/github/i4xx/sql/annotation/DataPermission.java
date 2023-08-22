package io.github.i4xx.sql.annotation;

import io.github.i4xx.sql.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    Scope[] query() default {};
    Scope[] modify() default {};

}
