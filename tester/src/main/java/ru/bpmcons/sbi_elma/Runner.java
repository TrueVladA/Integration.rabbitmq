package ru.bpmcons.sbi_elma;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.sender.RabbitMqSender;
import ru.bpmcons.sbi_elma.utils.RandomString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {
    private final RabbitMqSender rabbitMqSender;

    @Override
    public void run(String... args) throws Exception {
        buildAndSendMessage(args);
        System.exit(0);
    }

    private void buildAndSendMessage(String[] args) throws IOException {
        String id = UUID.randomUUID().toString();
        Message message = new Message(transformRequest(readRequest(args)).getBytes(StandardCharsets.UTF_8));
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
        message.getMessageProperties().setContentType("application/json");
        message.getMessageProperties().setMessageId(id);
        message.getMessageProperties().setCorrelationId(null);
        message.getMessageProperties().setReplyTo("document.eadoc.any.v1.rq.debug");
        message.getMessageProperties().setAppId("019011bd-eb86-7133-a0a3-2466acc5df0e");
        message.getMessageProperties().setTimestamp(Date.from(Instant.ofEpochMilli(1701168983311L)));
        message.getMessageProperties().getHeaders().put("version_api", "1.1.21");
        message.getMessageProperties().getHeaders().put("method", getMethod(args[0]));
        message.getMessageProperties().getHeaders().put("timestamp", "1701168983311");

        rabbitMqSender.send(message, "document.eadoc.any.v1.rq");
        System.out.println("Sent message with id " + id);
    }

    private String getMethod(String arg) {
        return switch (arg) {
            case "create" -> "CreateDoc";
            case "update" -> "UpdateDoc";
            case "get" -> "GetDoc";
            case "get_identity" -> "GetIdentityDoc";
            case "notification_upload" -> "NotificationUpload";
            case "search" -> "SearchDoc";
            case "create_identity" -> "CreateIdentityDoc";
            case "update_identity" -> "UpdateIdentityDoc";
            case "refresh_presign" -> "RefreshPresignUrl";
            case "delete_file" -> "DeleteFile";
            default -> throw new IllegalStateException("Method " + arg + " not found");
        };
    }

    private String readRequest(String... args) throws IOException {
        return Files.readString(Path.of("./requests", args[0], args[1] + ".json"));
    }

    private String transformRequest(String s) {
        return s.replace("${id_as_doc}", UUID.randomUUID().toString())
                .replace("${doc_series}", RandomString.documentId(5))
                .replace("${doc_number}", RandomString.documentId(9))
                .replace("${contract_series}", RandomString.documentId(4))
                .replace("${contract_number}", RandomString.documentId(8))
                .replace("${file_as_id}", Integer.toString(ThreadLocalRandom.current().nextInt()))
                .replace("${file_id}", UUID.randomUUID().toString());
    }
}
