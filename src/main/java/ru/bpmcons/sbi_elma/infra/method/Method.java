package ru.bpmcons.sbi_elma.infra.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Добавляет метод к контексту обработки. Метод может принимать только один тип аргумента,
 * который будет получен из входящего сообщения. Метод может вернуть значение через return,
 * либо вернуть его досрочно через {@link AsyncResultHandler}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Method {
    /**
     * SpEL-выражение, название метода
     * @return
     */
    String value();
}
