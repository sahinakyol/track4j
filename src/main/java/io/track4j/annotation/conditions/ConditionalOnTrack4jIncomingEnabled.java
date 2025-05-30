package io.track4j.annotation.conditions;

import io.track4j.beans.conditions.ConditionalOnTrack4jIncomingRequestEnabled;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ConditionalOnTrack4jIncomingRequestEnabled.class)
public @interface ConditionalOnTrack4jIncomingEnabled {
}
