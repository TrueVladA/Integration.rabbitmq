package ru.bpmcons.sbi_elma.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.bpmcons.sbi_elma.models.dto.docLock.ResponseFromEcmListBlock;

@Component
@Scope(value = "prototype")
public class ObjectMapperService {
    Logger logger = LoggerFactory.getLogger(ObjectMapperService.class);

    private final ObjectMapper objectMapper;

    public ObjectMapperService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @NonNull
    public <T> T getObjectFromJsonRequired(String json, @NonNull Class<T> clazz) {
        T object;
        try {
            object = objectMapper.readValue(json, clazz);
            if (clazz.equals(ResponseFromEcmListBlock.class)) {
                ResponseFromEcmListBlock o = (ResponseFromEcmListBlock) object;
                if (o.getResult().getResult() != null && o.getResult().getResult().length > 0) {
                    logger.debug(object.toString());
                }
            } else {
                logger.debug(object.toString());
            }
        } catch (JsonProcessingException e) {
            logger.error("При получении json модели произошла ошибка", e);
            throw new ru.bpmcons.sbi_elma.elma.exception.ObjectMapperException(e);
        }
        return object;
    }


    @NonNull
    public String getJsonFromObjectRequired(@NonNull Object object) {
        String rsl;
        try {
            rsl = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("При получении json модели произошла ошибка", e);
            throw new ru.bpmcons.sbi_elma.elma.exception.ObjectMapperException(e);
        }
        return rsl;
    }
}
