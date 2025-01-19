package ru.bpmcons.sbi_elma.exceptions;

public class PublicKeyNotFoundException extends ServiceResponseException {
    public PublicKeyNotFoundException(String system) {
        super(511, "Ошибка парсинга публичного ключа keycloak: ключ не найден для системы " + system);
    }
}
