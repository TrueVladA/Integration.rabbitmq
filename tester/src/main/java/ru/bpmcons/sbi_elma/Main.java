package ru.bpmcons.sbi_elma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.bpmcons.sbi_elma.sender.RabbitMqProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitMqProperties.class)
@EnableAspectJAutoProxy
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}