package ru.bpmcons.sbi_elma.infra.method;

/**
 * Данный обработчик конвертирует метод в асинхронный режим,
 * позволяя ему вернуть результат заренее и продолжить выполнение
 */
public interface AsyncResultHandler {
    void success(Object result);
}
