package ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryNameResolver;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.DictionaryKeyNormalizer;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.Normalize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DictionaryRepositoryMetadataParser {
    private final DictionaryNameResolver dictionaryNameResolver;

    public DictionaryRepositoryMetadataParser(BeanFactory beanFactory) {
        this.dictionaryNameResolver = new DictionaryNameResolver(beanFactory);
    }

    public RepositoryClassMetadata parseMetadata(Class<?> repository) {
        Class<?> entity = (Class<?>) ((ParameterizedType) repository.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return new RepositoryClassMetadata(
                entity,
                dictionaryNameResolver.resolveDictionaryName(entity),
                dictionaryNameResolver.resolveDisplayName(entity),
                Arrays.stream(ReflectionUtils.getAllDeclaredMethods(repository))
                        .filter(method -> !method.isDefault())
                        .collect(Collectors.toMap(
                                method -> method,
                                method -> parseMethod(method, entity),
                                (a, b) -> a
                        ))
        );
    }

    private MethodMetadata parseMethod(Method method, Class<?> entity) {
        String name = method.getName();
        if (!name.startsWith("find")) {
            throw new IllegalArgumentException("Method " + method + " for class " + method.getDeclaringClass() + " should start with 'find'");
        }
        name = name.substring("find".length());

        MethodMetadata.ReturnType returnType = MethodMetadata.ReturnType.SINGLE;
        if (name.startsWith("All")) {
            returnType = MethodMetadata.ReturnType.MULTIPLE;
            name = name.substring("All".length());
        } else if (name.endsWith("Optional") || Optional.class.isAssignableFrom(method.getReturnType())) {
            returnType = MethodMetadata.ReturnType.OPTIONAL;
            if (name.endsWith("Optional")) {
                name = name.substring(0, name.length() - "Optional".length());
            }
        }

        List<MethodMetadata.FilterItem> filter = new ArrayList<>();
        if (name.startsWith("By")) {
            name = name.substring("By".length());
            for (String fld : name.split("And")) {
                filter.add(parseFilterItem(fld, entity));
            }
        }

        DictionaryKeyNormalizer normalizer = key -> key;

        Normalize normalizeAnnotation = AnnotationUtils.findAnnotation(method, Normalize.class);
        if (normalizeAnnotation != null) {
            normalizer = createNormalizer(normalizeAnnotation.value());
        }

        return new MethodMetadata(method, returnType, filter, normalizer);
    }

    private MethodMetadata.FilterItem parseFilterItem(String fld, Class<?> entity) {
        String getterName = "get" + fld;
        Method method = ReflectionUtils.findMethod(entity, getterName);
        if (method == null) {
            throw new IllegalArgumentException("Not found getter " + getterName + " for entity" + entity);
        }
        return new MethodMetadata.FilterItem(
                fld,
                method
        );
    }

    private DictionaryKeyNormalizer createNormalizer(Class<?> type) {
        try {
            return (DictionaryKeyNormalizer) ReflectionUtils.accessibleConstructor(type).newInstance();
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find normalizer " + type + " constructor", e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create normalizer " + type, e);
        }
    }
}
