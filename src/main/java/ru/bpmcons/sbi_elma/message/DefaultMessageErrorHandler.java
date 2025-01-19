package ru.bpmcons.sbi_elma.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.bpmcons.sbi_elma.Versions;
import ru.bpmcons.sbi_elma.elma.exception.ElmaResponseException;
import ru.bpmcons.sbi_elma.elma.exception.ObjectMapperException;
import ru.bpmcons.sbi_elma.exceptions.EcmDocumentAwareException;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.message.logging.LoggingMessageFilter;
import ru.bpmcons.sbi_elma.message.logging.LoggingStage;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;
import ru.bpmcons.sbi_elma.properties.ResponseCodes;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultMessageErrorHandler implements MessageErrorHandler {
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;
    private final LoggingMessageFilter<CodeMessage> loggingMessageFilter;

    public DefaultMessageErrorHandler(MessageSender messageSender, ObjectMapper objectMapper) {
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.loggingMessageFilter = new LoggingMessageFilter<>(objectMapper, LoggingStage.ERROR);
    }

    @Override
    public void handleException(Exception exception, Object message) {
        ensureIncomingMessagePrint(message);
        if (exception instanceof HttpClientErrorException) {
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(ResponseCodes.BAD_REQUEST);
                codeMessage.setResponse_message(exception.getMessage());
            });
        } else if (exception instanceof EcmDocumentAwareException && MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(Versions.V_1_1_18))) { // TODO(EASED-1494) - remove version check when unused
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(String.valueOf(((ServiceResponseException) exception).getCode()));
                codeMessage.setResponse_message(exception.getMessage());
                codeMessage.setId_ecm_doc(((EcmDocumentAwareException) exception).getEcmId());
                codeMessage.setId_as_doc(((EcmDocumentAwareException) exception).getAsId());
            });
        } else if (exception instanceof ElmaResponseException) {
            log.error("Ошибка ELMA", exception);
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(String.valueOf(((ServiceResponseException) exception).getCode()));
                codeMessage.setResponse_message(exception.getMessage());
            });
        } else if (exception instanceof ServiceResponseException) {
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(String.valueOf(((ServiceResponseException) exception).getCode()));
                codeMessage.setResponse_message(exception.getMessage());
            });
        } else if (exception instanceof ConstraintViolationException) {
            String msg = ((ConstraintViolationException) exception).getConstraintViolations().stream()
                    .map(this::formatConstraintViolation)
                    .collect(Collectors.joining(", "));
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(ResponseCodes.REQUIRED_VALUE_MISSING);
                codeMessage.setResponse_message("Ошибки валидации: " + msg);
            });
        } else {
            sendResponse(codeMessage -> {
                codeMessage.setResponse_code(ResponseCodes.INTERNAL_MODULE_ERROR);
                codeMessage.setResponse_message(exception.getClass().getSimpleName() + ": " + exception.getMessage());
            });
            log.error("Ошибка модуля", exception);
        }
    }

    @Override
    public void logException(Exception exception, Object message) {
        ensureIncomingMessagePrint(message);

        log.error("Ошибка модуля", exception);
    }

    /**
     * Выводим входящее сообщение, если ещё не вывели
     */
    private void ensureIncomingMessagePrint(Object message) {
        if (LoggingMessageFilter.getCurrentStage() == null) {
            if (message instanceof Message) {
                String body = new String(((Message) message).getBody(), StandardCharsets.UTF_8);
                log.info(((Message) message).getMessageProperties().toString() + " ::: " + body);
            } else {
                log.info(message.toString());
            }
        }
    }

    private void sendResponse(Consumer<CodeMessage> enricher) {
        CodeMessage codeMessage = new CodeMessage();
        enricher.accept(codeMessage);
        codeMessage.setRquid(MessagePropertiesHolder.getMessageProperties().getMessageId());
        loggingMessageFilter.filter(codeMessage);
        try {
            messageSender.handle(objectMapper.writeValueAsString(codeMessage));
        } catch (JsonProcessingException e) {
            throw new ObjectMapperException(e);
        }
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        int nskip = 0;
        StringBuilder builder = new StringBuilder();
        for (Path.Node node : violation.getPropertyPath()) {
            nskip++;
            if (nskip < 3) {
                continue;
            }
            builder.append(node.getName()).append('.');
        }
        return builder.substring(0, builder.length() - 1) + ": " + violation.getMessage();
    }
}
