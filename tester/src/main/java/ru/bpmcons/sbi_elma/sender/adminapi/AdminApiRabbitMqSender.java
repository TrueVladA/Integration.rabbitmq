package ru.bpmcons.sbi_elma.sender.adminapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.sender.RabbitMqProperties;
import ru.bpmcons.sbi_elma.sender.RabbitMqSender;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;

@RequiredArgsConstructor
public class AdminApiRabbitMqSender implements RabbitMqSender {
    private final RestTemplate restTemplate;
    private final RabbitMqProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, KeyManagementException {
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    @SneakyThrows
    @Override
    public void send(Message message, String routingKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(properties.getUsername(), properties.getPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectNode objectNode = buildProperties(message);
        SendRequest request = new SendRequest(objectNode, routingKey, Base64.getEncoder().encodeToString(message.getBody()), "base64");
        URI url = URI.create("https://"
                + properties.getHost()
                + ":"
                + properties.getPort()
                + "/api/exchanges/"
                + URLEncoder.encode(properties.getVirtualHost(), StandardCharsets.UTF_8)
                + "/amq.default/publish"
        );
        restTemplate.exchange(
                new RequestEntity<>(
                        objectMapper.writeValueAsString(request),
                        headers,
                        HttpMethod.POST,
                        url
                ),
                String.class
        );
    }

    private ObjectNode buildProperties(Message message) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("delivery_mode", MessageDeliveryMode.toInt(message.getMessageProperties().getDeliveryMode()));
        objectNode.put("content_type", message.getMessageProperties().getContentType());
        objectNode.put("message_id", message.getMessageProperties().getMessageId());
        objectNode.put("reply_to", message.getMessageProperties().getReplyTo());
        objectNode.put("app_id", message.getMessageProperties().getAppId());
        objectNode.put("timestamp", message.getMessageProperties().getTimestamp().getTime());
        objectNode.set("headers", objectMapper.valueToTree(message.getMessageProperties().getHeaders()));
        return objectNode;
    }


    private record SendRequest(
            Object properties,
            @JsonProperty("routing_key")
            String routingKey,
            String payload,
            @JsonProperty("payload_encoding")
            String payloadEncoding
    ) {}
}
