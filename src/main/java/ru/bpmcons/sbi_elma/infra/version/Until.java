package ru.bpmcons.sbi_elma.infra.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Until {
    int major() default 0;
    int minor() default 0;
    int patch() default 0;
}
