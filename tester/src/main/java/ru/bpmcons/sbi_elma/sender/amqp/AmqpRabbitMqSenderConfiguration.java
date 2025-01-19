package ru.bpmcons.sbi_elma.sender.amqp;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bpmcons.sbi_elma.sender.RabbitMqProperties;
import ru.bpmcons.sbi_elma.sender.RabbitMqSender;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.mode", havingValue = "amqp", matchIfMissing = true)
public class AmqpRabbitMqSenderConfiguration {

    @Bean
    public CachingConnectionFactory connectionFactory(RabbitMqProperties properties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        String url = "amqp://" + properties.getUsername()
                + ":" + properties.getPassword()
                + "@" + properties.getHost()
                + ":" + properties.getPort();
        connectionFactory.setUri(url);
        connectionFactory.setVirtualHost(properties.getVirtualHost());
        return connectionFactory;
    }

    @Bean
    public RabbitMqSender amqpRabbitSender(RabbitTemplate template, RabbitMqProperties properties) {
        return new AmqpRabbitMqSender(template, properties);
    }
}
