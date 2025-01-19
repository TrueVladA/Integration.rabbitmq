package ru.bpmcons.sbi_elma.security.authorization;

import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorized {
    OperationName value();
}
