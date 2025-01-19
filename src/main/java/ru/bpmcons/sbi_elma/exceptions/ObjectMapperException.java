package ru.bpmcons.sbi_elma.exceptions;

import lombok.Getter;
import org.springframework.amqp.core.MessageProperties;

@Getter
public class ObjectMapperException extends RuntimeException {
    private String responseMessage;
    private MessageProperties messageProperties;

    public ObjectMapperException(String responseMessage, MessageProperties messageProperties) {
        this.responseMessage = responseMessage;
        this.messageProperties = messageProperties;
    }

    public String getResponseMessage() {
        return "Процесс парсинга JSON был прерван по следующей причине: "
                + responseMessage;
    }
}
