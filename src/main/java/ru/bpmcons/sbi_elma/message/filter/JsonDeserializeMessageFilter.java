package ru.bpmcons.sbi_elma.message.filter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.infra.version.JacksonVersionFilter;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.message.MessageTypeResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Фильтр десериализует сообщение кролика в объект
 */
@RequiredArgsConstructor
public class JsonDeserializeMessageFilter implements MessageFilter<Message, Object> {
    private final MessageTypeResolver messageTypeResolver;
    private final ObjectMapper objectMapper;

    @Override
    public Object filter(Message message) {
        Class<?> type = messageTypeResolver.resolveType(message);
        try {
            if (message.getBody() == null || message.getBody().length == 0) {
                throw new Exception("Тело сообщения пустое");
            }
            ObjectMapper mapper = objectMapper.copy();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
            Version version = MessagePropertiesHolder.getRequiredVersion();
            mapper.setAnnotationIntrospector(
                    AnnotationIntrospector.pair(mapper.getDeserializationConfig().getAnnotationIntrospector(), new JacksonVersionFilter(version))
            );
            return mapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8), type);
        } catch (JsonProcessingException e) {
            throw new Exception(e);
        }
    }

    public static final class Exception extends ServiceResponseException {
        public Exception(String message) {
            super(HttpStatus.BAD_REQUEST.value(), message);
        }

        public Exception(IOException e) {
            super(HttpStatus.BAD_REQUEST.value(), "Ошибка разбора JSON-запроса: " + e.getMessage());
        }
    }
}
