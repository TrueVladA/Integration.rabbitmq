package ru.bpmcons.sbi_elma.message.logging;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.infra.version.JacksonVersionFilter;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;

@Slf4j
public class LoggingMessageFilter<M> implements MessageFilter<M, M> {
    private static final FilterProvider FILTERS = new SimpleFilterProvider()
            .setFailOnUnknownId(false)
            .setDefaultFilter(new HideFromLogs.Filter());

    private static final ThreadLocal<LoggingStage> CURRENT_STAGE = ThreadLocal.withInitial(() -> null);

    private final ObjectMapper objectMapper;
    private final LoggingStage loggingStage;

    public LoggingMessageFilter(ObjectMapper objectMapper, LoggingStage loggingStage) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.setAnnotationIntrospectors(
                new AnnotationIntrospectorPair(
                        this.objectMapper.getSerializationConfig().getAnnotationIntrospector(),
                        new HideFromLogs.AnnotationIntrospector()
                ),
                new AnnotationIntrospectorPair(
                        this.objectMapper.getDeserializationConfig().getAnnotationIntrospector(),
                        new HideFromLogs.AnnotationIntrospector()
                )
        );
        this.loggingStage = loggingStage;
    }

    @Override
    public M filter(M message) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
            Version version = MessagePropertiesHolder.getRequiredVersion();
            mapper.setAnnotationIntrospector(
                    AnnotationIntrospector.pair(mapper.getSerializationConfig().getAnnotationIntrospector(), new JacksonVersionFilter(version))
            );
            String msg = mapper.writer(FILTERS).writeValueAsString(message);
            CURRENT_STAGE.set(loggingStage);
            switch (loggingStage) {
                case REQUEST:
                    log.info(msg);
                    break;
                case RESPONSE:
                case ERROR:
                    log.info("Ответ на запрос\":" + "{\"reply_to\":" + "\"" + MessagePropertiesHolder.getMessageProperties().getReplyTo() + ",\"payload\":" + msg + "}");
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for logs", e);
        }

        return message;
    }

    @Nullable
    public static LoggingStage getCurrentStage() {
        return CURRENT_STAGE.get();
    }
}
