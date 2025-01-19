package ru.bpmcons.sbi_elma.sender.amqp;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.bpmcons.sbi_elma.sender.RabbitMqProperties;
import ru.bpmcons.sbi_elma.sender.RabbitMqSender;

@RequiredArgsConstructor
public class AmqpRabbitMqSender implements RabbitMqSender {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    @Override
    public void send(Message message, String routingKey) {
        rabbitTemplate.send(properties.getQueue(), routingKey, message);
    }
}
