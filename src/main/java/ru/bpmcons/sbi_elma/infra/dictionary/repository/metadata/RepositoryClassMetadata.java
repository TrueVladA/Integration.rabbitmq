package ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;

@Data
public class RepositoryClassMetadata {
    private final Class<?> entity;
    private final String dictName;
    private final String displayName;
    private final Map<Method, MethodMetadata> methods;
}
