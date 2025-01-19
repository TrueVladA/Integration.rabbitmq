package ru.bpmcons.sbi_elma.infra.message;

/**
 * Обработчик сообщения
 * @param <M> входящее сообщение
 */
public interface MessageHandler<M> {
    void handle(M message);
}
