package ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.DictionaryKeyNormalizer;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MethodMetadata {
    private final Method method;
    private final ReturnType returnType;
    private final List<FilterItem> filter;
    private final DictionaryKeyNormalizer normalizer;

    @Data
    public static class FilterItem {
        private final String name;
        private final Method getter;

        public Object getValue(Object entity) {
            return ReflectionUtils.invokeMethod(getter, entity);
        }
    }

    public enum ReturnType {
        SINGLE,
        MULTIPLE,
        OPTIONAL
    }
}
