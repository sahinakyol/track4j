package io.track4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Track4j {
    String name() default "";
    boolean includeArgs() default true;
    boolean includeResult() default true;
    boolean enabled() default true;
    String[] tags() default {};
}