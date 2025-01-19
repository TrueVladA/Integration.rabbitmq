package ru.bpmcons.sbi_elma.message;

import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.infra.message.MessageHandler;
import ru.bpmcons.sbi_elma.infra.message.MessageHandlerChain;
import ru.bpmcons.sbi_elma.infra.method.MethodRegistry;

@Service
public class MethodProxy implements MessageTypeResolver, MessageHandler<Object> {
    private final MethodRegistry methodRegistry;
    private final MessageErrorHandler errorHandler;
    @Qualifier("response")
    private final MessageHandlerChain messageHandlerChain;

    public MethodProxy(MethodRegistry methodRegistry, MessageErrorHandler errorHandler, @Qualifier("response") MessageHandlerChain messageHandlerChain) {
        this.methodRegistry = methodRegistry;
        this.errorHandler = errorHandler;
        this.messageHandlerChain = messageHandlerChain;
    }

    @Override
    public Class<?> resolveType(Message message) {
        return methodRegistry.getArgumentType(MessagePropertiesHolder.getMethodName(), MessagePropertiesHolder.getVersion());
    }

    @Override
    public void handle(Object message) {
        methodRegistry.invoke(MessagePropertiesHolder.getMethodName(), MessagePropertiesHolder.getVersion(), message, messageHandlerChain::handle, errorHandler);
    }
}
