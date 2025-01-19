package ru.bpmcons.sbi_elma.sender;

import org.springframework.amqp.core.Message;

public interface RabbitMqSender {
    void send(Message message, String routingKey);
}
