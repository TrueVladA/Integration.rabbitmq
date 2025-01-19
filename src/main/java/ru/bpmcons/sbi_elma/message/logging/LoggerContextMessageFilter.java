package ru.bpmcons.sbi_elma.message.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.properties.MessageHeaderNames;

@RequiredArgsConstructor
public class LoggerContextMessageFilter implements MessageFilter<Message, Message> {
    private final MessageHeaderNames messageHeaderNames;

    @Override
    public Message filter(Message message) {
        LoggerContextLayout.set("messageId", message.getMessageProperties().getMessageId());
        LoggerContextLayout.set("method", message.getMessageProperties().getHeader(messageHeaderNames.getMethod()));
        LoggerContextLayout.set("correlationId", message.getMessageProperties().getCorrelationId());
        LoggerContextLayout.set("versionApi", message.getMessageProperties().getHeader(messageHeaderNames.getVersionApi()));
        return message;
    }

    @Override
    public void cleanupAfterHandle() {
        LoggerContextLayout.reset();
    }
}
