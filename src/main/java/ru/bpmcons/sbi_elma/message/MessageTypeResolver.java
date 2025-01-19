package ru.bpmcons.sbi_elma.message;

import org.springframework.amqp.core.Message;
import org.springframework.lang.NonNull;

/**
 * Резолвер типа сообщения по сообщению из кролика. Используется в {@link ru.bpmcons.sbi_elma.message.filter.JsonDeserializeMessageFilter}
 */
public interface MessageTypeResolver {
    /**
     * Получить тип по сообщению
     * @param message сообщение кролика
     * @return тип
     */
    @NonNull
    Class<?> resolveType(@NonNull Message message);
}
