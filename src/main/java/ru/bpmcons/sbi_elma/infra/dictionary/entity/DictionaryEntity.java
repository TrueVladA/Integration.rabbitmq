package ru.bpmcons.sbi_elma.infra.dictionary.entity;

import java.lang.annotation.*;

/**
 * Индикатор сущности справочника. Также указывает метаданные справочника
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DictionaryEntity {
    /**
     * Имя приложения в ECM
     */
    String name();

    /**
     * Человекочитаемое имя словаря
     */
    String displayName();
}
