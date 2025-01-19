package ru.bpmcons.sbi_elma.message;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.infra.message.MessageHandler;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.MessageHeaderNames;
import ru.bpmcons.sbi_elma.properties.RabbitMqConst;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class MessageSender implements MessageHandler<String>, ApplicationContextAware {
    private final MessageHeaderNames messageHeaderNames;
    private final RabbitMqConst rabbitMqConst;
    private final EcmProperties ecmProperties;
    private final RabbitTemplate rabbitTemplate;
    @Setter
    private ApplicationContext applicationContext;

    public MessageSender(MessageHeaderNames messageHeaderNames, RabbitMqConst rabbitMqConst, EcmProperties ecmProperties, RabbitTemplate rabbitTemplate) {
        this.messageHeaderNames = messageHeaderNames;
        this.rabbitMqConst = rabbitMqConst;
        this.ecmProperties = ecmProperties;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void handle(String message) {
        MessageProperties origin = MessagePropertiesHolder.getMessageProperties();

        MessageProperties response = new MessageProperties();
        response.setReplyTo("document.transform.any.v1.rq");
        response.setType("application/json");
        response.setAppId(origin.getAppId());
        response.setTimestamp(new Date());
        response.setCorrelationId(origin.getMessageId());
        response.setMessageId(UUID.randomUUID().toString());
        response.setContentType("application/json");
        response.setHeader(messageHeaderNames.getVersionApi(), ecmProperties.getVersionApi());
        response.setHeader("account", origin.getHeaders().get("account")); // todo deprecated, delete after EASED-1484 in prod
        response.setHeader(messageHeaderNames.getMethod(), origin.getHeaders().get(messageHeaderNames.getMethod()));
        response.setHeader(messageHeaderNames.getRequestStart(), RequestTimingTracker.getStartTime().toInstant().toEpochMilli());
        response.setHeader(messageHeaderNames.getRequestFinish(), System.currentTimeMillis());

        synchronized (this) {
            while (true) {
                try {
//                    if (origin.getHeaders().get("test") != null) { использовалось для тестов, закоменченно 11-03-2024 18:14
//                        rabbitTemplateTest.convertAndSend(rabbitMqConst.getResponseExchange(), origin.getReplyTo(), new Message(message.getBytes(StandardCharsets.UTF_8), response));
//                    } else {
                    rabbitTemplate.convertAndSend(rabbitMqConst.getResponseExchange(), origin.getReplyTo(), new Message(message.getBytes(StandardCharsets.UTF_8), response));
//                    }
                    break;
                } catch (AmqpException e) {
                    log.error("Ошибка отправки сообщения в RabbitMQ. Получение сообщений остановлено", e);
                    try {
                        log.warn("Повтор отправки собщения через " + rabbitMqConst.getRetryTimeout());
                        Thread.sleep(rabbitMqConst.getRetryTimeout().toMillis());
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }
}
