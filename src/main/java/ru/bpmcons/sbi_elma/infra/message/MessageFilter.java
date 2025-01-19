package ru.bpmcons.sbi_elma.infra.message;

import org.springframework.lang.NonNull;

/**
 * Фильтр/конвертер сообщения
 * @param <I> входящий тип сообщения
 * @param <O> исходящий тип сообщения
 */
public interface MessageFilter<I, O> {
    /**
     * Выполнить фильтрацию
     * @param message входящее сообщение
     * @return исходящее сообщение
     */
    @NonNull
    O filter(@NonNull I message);

    /**
     * Очистить контекст после обработки сообщения. Вызывается в любом случае. В этот момент {@link #filter(Object)} может быть не вызван
     */
    default void cleanupAfterHandle() {}
}
