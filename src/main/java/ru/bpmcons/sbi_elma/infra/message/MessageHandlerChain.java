package ru.bpmcons.sbi_elma.infra.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Цепочка обработки сообщения
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MessageHandlerChain implements MessageHandler<Object> {
    private final List<MessageFilter<Object, Object>> filters;
    private final MessageHandler<Object> handler;
    private final MessageErrorHandler errorHandler;

    @Override
    public void handle(@NonNull Object message) {
        Object msg = message;
        try {
            for (MessageFilter<Object, Object> filter : filters) {
                msg = filter.filter(msg);
            }
            handler.handle(msg);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.handleException(e, msg);
            } else {
                throw e;
            }
        } finally {
            for (MessageFilter<Object, Object> filter : filters) {
                filter.cleanupAfterHandle();
            }
        }
    }

    public static class Builder<I> {
        private final List<MessageFilter<Object, Object>> filters = new ArrayList<>();
        private MessageErrorHandler errorHandler = null;
        public Builder() {}

        @SuppressWarnings("unchecked")
        public <O> Builder<O> and(MessageFilter<I, O> filter) {
            this.filters.add((MessageFilter<Object, Object>) filter);
            return (Builder<O>) this;
        }

        public Builder<I> errorHandler(MessageErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @SuppressWarnings("unchecked")
        public MessageHandlerChain build(MessageHandler<I> handler) {
            return new MessageHandlerChain(filters, (MessageHandler<Object>) handler, errorHandler);
        }
    }
}
