package ru.bpmcons.sbi_elma.message.filter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ru.bpmcons.sbi_elma.elma.exception.ObjectMapperException;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.infra.version.JacksonVersionFilter;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;

/**
 * Фильтр сериализует сообщение в JSON
 */
@RequiredArgsConstructor
public class JsonSerializeMessageFilter implements MessageFilter<Object, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String filter(Object message) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
            Version version = MessagePropertiesHolder.getRequiredVersion();
            mapper.setAnnotationIntrospector(
                    AnnotationIntrospector.pair(mapper.getSerializationConfig().getAnnotationIntrospector(), new JacksonVersionFilter(version))
            );
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperException(e);
        }
    }
}
