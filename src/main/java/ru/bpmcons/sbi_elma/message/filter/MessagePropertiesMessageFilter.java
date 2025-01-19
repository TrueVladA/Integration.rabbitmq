package ru.bpmcons.sbi_elma.message.filter;

import org.springframework.amqp.core.Message;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;

/**
 * Фильтр, управляющий {@link MessagePropertiesHolder}
 */
public class MessagePropertiesMessageFilter implements MessageFilter<Message, Message> {
    @Override
    public Message filter(Message message) {
        MessagePropertiesHolder.setProperties(message.getMessageProperties());
        return message;
    }

    @Override
    public void cleanupAfterHandle() {
        MessagePropertiesHolder.resetProperties();
    }
}
