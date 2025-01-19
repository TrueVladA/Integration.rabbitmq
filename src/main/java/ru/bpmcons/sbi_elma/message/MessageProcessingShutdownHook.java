package ru.bpmcons.sbi_elma.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MessageProcessingShutdownHook {
    private final SimpleMessageListenerContainer listenerContainer;
    @Qualifier("messageExecutor")
    private final ForkJoinPool executorService;

    public MessageProcessingShutdownHook(SimpleMessageListenerContainer listenerContainer, @Qualifier("messageExecutor") ForkJoinPool executorService) {
        this.listenerContainer = listenerContainer;
        this.executorService = executorService;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Выключаемся...");
        listenerContainer.stop();
        log.info("Приём сообщений из RabbitMQ остановлен");
        while (executorService.getQueuedSubmissionCount() > 0) {
            log.info("Ждём, пока очередь FJP очистится, пул: " + executorService);
            Thread.sleep(500); // ждём, пока listener отправит все сообщения в очередь
        }
        executorService.shutdown();
        log.info("Получение сообщений остановлено");
        Instant start = Instant.now();
        do {
            long elapsedSec = Duration.between(start, Instant.now()).toSeconds();
            log.info("Прошло " + elapsedSec + " секунд, пул: " + executorService);
        } while (!executorService.awaitTermination(1, TimeUnit.SECONDS));
        log.info("Обработка сообщений завершилась за " + Duration.between(start, Instant.now()).toSeconds() + " секунд");
    }
}
