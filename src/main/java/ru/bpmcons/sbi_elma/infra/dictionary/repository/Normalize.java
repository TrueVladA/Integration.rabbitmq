package ru.bpmcons.sbi_elma.infra.dictionary.repository;

import ru.bpmcons.sbi_elma.infra.dictionary.normalize.DictionaryKeyNormalizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Номализирует ключи при поиске сущности в репозитории
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Normalize {
    /**
     * Класс нормализатора
     */
    Class<? extends DictionaryKeyNormalizer> value();
}
