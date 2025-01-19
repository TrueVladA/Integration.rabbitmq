package ru.bpmcons.sbi_elma.infra.message;

import org.springframework.lang.NonNull;

/**
 * Обработчик исключений для {@link MessageHandlerChain}
 *
 * @see MessageHandlerChain
 */
public interface MessageErrorHandler {
    /**
     * Обработать ошибку
     *
     * @param exception ошибка
     */
    void handleException(@NonNull Exception exception, @NonNull Object message);

    /**
     * Выводит ошибку в консоль
     *
     * @param exception ошибка
     */
    void logException(@NonNull Exception exception, @NonNull Object message);
}
