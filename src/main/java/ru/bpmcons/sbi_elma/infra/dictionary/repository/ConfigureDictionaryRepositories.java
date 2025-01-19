package ru.bpmcons.sbi_elma.infra.dictionary.repository;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Включает поиск репозиториев словарей в конкретной папке
 * @see DictionaryRepository
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DictionaryRepositoryBeanDefinitionRegistrar.class)
public @interface ConfigureDictionaryRepositories {
}
