package ru.bpmcons.sbi_elma.message.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;
import ru.bpmcons.sbi_elma.feature.FeatureFlags;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.properties.MessageHeaderNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Фильтр проверяет и парсит наличие всех обязательных property и headers. Если чего-то нет, кидает ошибку
 */
@RequiredArgsConstructor
public class ParsePropertiesMessageFilter implements MessageFilter<Message, Message> {
    private final MessageHeaderNames messageHeaderNames;

    @Override
    public Message filter(Message message) {
        MessageProperties messageProperties = message.getMessageProperties();

        List<String> missing = findMissingProperties(messageProperties);
        if (!missing.isEmpty()) {
            throw new Exception(missing);
        }
        MessagePropertiesHolder.setParsedProperties(
                Version.parse(messageProperties.getHeader(messageHeaderNames.getVersionApi())),
                messageProperties.getHeader(messageHeaderNames.getMethod()),
                messageProperties.getHeader(messageHeaderNames.getFeatures()) == null
                        ? Collections.emptyList()
                        : Arrays.stream(messageProperties.getHeader(messageHeaderNames.getFeatures()).toString().split(","))
                                .map(FeatureFlags::parse)
                                .collect(Collectors.toList())
        );

        return message;
    }

    @Override
    public void cleanupAfterHandle() {
        MessagePropertiesHolder.resetParsedProperties();
    }

    private List<String> findMissingProperties(MessageProperties messageProperties) {
        List<String> missing = new ArrayList<>();
        if (messageProperties.<String>getHeader(messageHeaderNames.getMethod()) == null) {
            missing.add(messageHeaderNames.getMethod());
        }
        if (messageProperties.<String>getHeader(messageHeaderNames.getVersionApi()) == null) {
            missing.add(messageHeaderNames.getVersionApi());
        }

        if (messageProperties.getAppId() == null) {
            missing.add("appId");
        }
        if (messageProperties.getContentType() == null) {
            missing.add("contentType");
        }
        if (messageProperties.getTimestamp() == null) {
            missing.add("timestamp");
        }
        if (messageProperties.getReplyTo() == null) {
            missing.add("replyTo");
        }
        if (messageProperties.getMessageId() == null) {
            missing.add("messageId");
        }
        return missing;
    }

    public static class Exception extends ServiceResponseException {

        protected Exception(List<String> missing) {
            super(416, "В properties не хватает:" +
                    missing.stream().reduce((s, s2) -> s + ", " + s2).orElse(""));
        }
    }
}
