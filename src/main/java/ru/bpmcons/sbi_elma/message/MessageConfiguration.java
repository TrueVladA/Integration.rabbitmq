package ru.bpmcons.sbi_elma.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.infra.message.MessageHandlerChain;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtParser;
import ru.bpmcons.sbi_elma.message.authentication.AuthenticatingMessageFilter;
import ru.bpmcons.sbi_elma.message.filter.*;
import ru.bpmcons.sbi_elma.message.logging.IdentifiableLoggingMessageFilter;
import ru.bpmcons.sbi_elma.message.logging.LoggerContextMessageFilter;
import ru.bpmcons.sbi_elma.message.logging.LoggingMessageFilter;
import ru.bpmcons.sbi_elma.message.logging.LoggingStage;
import ru.bpmcons.sbi_elma.properties.MessageHeaderNames;
import ru.bpmcons.sbi_elma.properties.RabbitMqConst;
import ru.bpmcons.sbi_elma.properties.SettingsProperties;
import ru.bpmcons.sbi_elma.security.MessageAuthenticator;

import java.util.concurrent.ForkJoinPool;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageConfiguration {
    private final MessageHeaderNames messageHeaderNames;
    private final MessageAuthenticator authenticator;
    private final ObjectMapper objectMapper;
    private final RabbitMqConst rabbitMqConst;
    private final KeycloakJwtParser keycloakJwtParser;
    private final SettingsProperties settingsProperties;

    @Bean
    @Qualifier("messageExecutor")
    public ForkJoinPool messageExecutorService() {
        return new ForkJoinPool(rabbitMqConst.getParallelism());
    }

    @Bean
    @Qualifier("messageExecutorWorker")
    public ForkJoinPool messageExecutorWorkerService() {
        return new ForkJoinPool(rabbitMqConst.getWorkerParallelism());
    }

    @Bean
    public MessageHandlerChain messageHandlerChain(MessageErrorHandler messageErrorHandler, MethodProxy methodProxy) {
        return new MessageHandlerChain.Builder<Message>()
                .and(new LoggerContextMessageFilter(messageHeaderNames))
                .and(new RequestTimingTrackerMessageFilter<>())
                .and(new MessagePropertiesMessageFilter())
                .and(new ParsePropertiesMessageFilter(messageHeaderNames))
                .and(new SecurityContextMessageFilter(authenticator))
                .and(new JsonDeserializeMessageFilter(methodProxy, objectMapper))
                .and(new IdentifiableLoggingMessageFilter())
                .and(new LoggingMessageFilter<>(objectMapper, LoggingStage.REQUEST))
                .and(new AuthenticatingMessageFilter(keycloakJwtParser, settingsProperties))
                .errorHandler(messageErrorHandler)
                .build(methodProxy);
    }

    @Bean
    @Qualifier("response")
    public MessageHandlerChain responseChain(MessageSender messageSender, MessageErrorHandler messageErrorHandler) {
        return new MessageHandlerChain.Builder<>()
                .and(new LoggingMessageFilter<>(objectMapper, LoggingStage.RESPONSE))
                .and(new JsonSerializeMessageFilter(objectMapper))
                .errorHandler(messageErrorHandler)
                .build(messageSender);
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        String url = "amqp://" + rabbitMqConst.getUsername()
                + ":" + rabbitMqConst.getPassword()
                + "@" + rabbitMqConst.getHost()
                + ":" + rabbitMqConst.getPort();
        connectionFactory.setUri(url);
        log.info("Подключение к rabbitMQ успешно выполнено. host: " + connectionFactory.getHost() + " queue: " + rabbitMqConst.getRequestQueue());
        return connectionFactory;
    }

//    @Bean
//    @Profile(value = "test")
//    public CachingConnectionFactory connectionFactoryTest() {
//        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        String url = "amqp://" + rabbitMqConst.getUsernameTest()
//                + ":" + rabbitMqConst.getPasswordTest()
//                + "@" + rabbitMqConst.getHost()
//                + ":" + rabbitMqConst.getPort()
//                + "/" + "%2fEADOC";
//        cachingConnectionFactory.setUri(url);
//        log.info("Подключение к rabbitMQ успешно выполнено. host: " + cachingConnectionFactory.getHost() + " queue: " + rabbitMqConst.getRequestQueue());
//        return cachingConnectionFactory;
//    }

    @Bean
    public SimpleMessageListenerContainer rabbitContainer(MessageErrorHandler messageErrorHandler, MethodProxy methodProxy) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(rabbitMqConst.getRequestQueue());
        container.setMessageListener(message -> {
            ForkJoinPool executor = messageExecutorService();

            while (executor.getQueuedSubmissionCount() > rabbitMqConst.getParallelism()) {
                try {
                    Thread.sleep(rabbitMqConst.getThrottlerQueueRecheckTimeout().toMillis());
                } catch (InterruptedException e) {
                    break;
                }
            }

            executor.submit(() ->
                    messageHandlerChain(messageErrorHandler, methodProxy).handle(message));
        });
        container.setPrefetchCount(rabbitMqConst.getPrefetchCount());
        return container;
    }

//    @Bean
//    @Profile(value = "test")
//    public SimpleMessageListenerContainer rabbitContainerTest(MessageErrorHandler messageErrorHandler, MethodProxy methodProxy) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactoryTest());
//        container.setQueueNames(rabbitMqConst.getRequestQueue());
//        container.setMessageListener(message -> executorService.submit(() ->
//                messageHandlerChain(messageErrorHandler, methodProxy).handle(message)));
//        container.setPrefetchCount(rabbitMqConst.getPrefetchCount());
//        return container;
//    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        return rabbitTemplate;
    }

//    @Bean
//    @Profile(value = "test")
//    @Qualifier("rabbitTemplateTest")
//    public RabbitTemplate rabbitTemplateTest() {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate();
//        rabbitTemplate.setConnectionFactory(connectionFactoryTest());
//        return rabbitTemplate;
//    }
}
