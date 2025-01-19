package ru.bpmcons.sbi_elma.sender.adminapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.sender.RabbitMqProperties;
import ru.bpmcons.sbi_elma.sender.RabbitMqSender;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.mode", havingValue = "admin_api")
public class AdminApiRabbitMqSenderConfiguration {
    @Bean
    public RabbitMqSender adminApiRabbitSender(RabbitMqProperties properties) {
        return new AdminApiRabbitMqSender(new RestTemplate(), properties);
    }
}
