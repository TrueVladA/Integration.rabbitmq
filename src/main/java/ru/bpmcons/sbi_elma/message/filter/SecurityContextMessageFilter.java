package ru.bpmcons.sbi_elma.message.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.security.MessageAuthenticator;
import ru.bpmcons.sbi_elma.security.SecurityContext;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

/**
 * Фильтр, управляющий {@link SecurityContextHolder}
 */
@RequiredArgsConstructor
public class SecurityContextMessageFilter implements MessageFilter<Message, Message> {
    private final MessageAuthenticator messageAuthenticator;

    @Override
    public Message filter(Message message) {
        SecurityContext ctx = messageAuthenticator.authenticate(message.getMessageProperties());
        SecurityContextHolder.setContext(ctx);
        return message;
    }

    @Override
    public void cleanupAfterHandle() {
        SecurityContextHolder.resetContext();
    }
}
