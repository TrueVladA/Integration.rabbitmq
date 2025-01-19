package ru.bpmcons.sbi_elma.infra.dictionary.entity;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringValueResolver;

public class DictionaryNameResolver {
    private final StringValueResolver valueResolver;

    public DictionaryNameResolver(BeanFactory beanFactory) {
        this.valueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
    }

    public String resolveDictionaryName(Class<?> entityClass) {
        DictionaryEntity dictAnnotation = AnnotationUtils.findAnnotation(entityClass, DictionaryEntity.class);
        if (dictAnnotation == null) {
            throw new IllegalArgumentException("Class " + entityClass + " should have @DictionaryEntity annotation");
        }
        String nameRaw = dictAnnotation.name();
        String name = valueResolver.resolveStringValue(nameRaw);
        if (name == null) {
            throw new IllegalStateException("Resolved dict name is null for " + entityClass);
        }
        return name;
    }

    public String resolveDisplayName(Class<?> entityClass) {
        DictionaryEntity dictAnnotation = AnnotationUtils.findAnnotation(entityClass, DictionaryEntity.class);
        if (dictAnnotation == null) {
            throw new IllegalArgumentException("Class " + entityClass + " should have @DictionaryEntity annotation");
        }
        return dictAnnotation.displayName();
    }
}
