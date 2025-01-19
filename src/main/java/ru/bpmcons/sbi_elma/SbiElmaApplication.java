package ru.bpmcons.sbi_elma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.handlers.BadRequestErrorHandler;
import ru.bpmcons.sbi_elma.properties.EcmProperties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Модуль интеграции
 */
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class SbiElmaApplication {
    Logger logger = LoggerFactory.getLogger(SbiElmaApplication.class);
    @Value( "${settings.threadsnumber}" )
    private String nThreads;

    public SbiElmaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(SbiElmaApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(MapperFeature.DEFAULT_VIEW_INCLUSION);
//        objectMapper.setDateFormat(dateFormat);
//        objectMapper.findAndRegisterModules();
//        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//        return builder -> {
//            builder.simpleDateFormat(dateTimeFormat);
//            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormat)));
//            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
//            builder.timeZone(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
//            builder.configure(objectMapper());
//        };
//    }


//    @Bean
//    @Scope(value = "prototype")
//    public ConnectionFactory connectionFactory() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        String url = "amqp://" + rabbitMqConst.getUsername()
//                + ":" + rabbitMqConst.getPassword()
//                + "@" + rabbitMqConst.getHost()
//                + ":" + rabbitMqConst.getPort()
//                + "/" + rabbitMqConst.getVirtualhost();
//        connectionFactory.setUri(url);
//        String virtualHost = connectionFactory.getVirtualHost();
//        String host = connectionFactory.getHost();
//        int port = connectionFactory.getPort();
//        String username = connectionFactory.getUsername();
//        String password = connectionFactory.getPassword();
//        logger.info("host " + host);
//        logger.info("port " + port);
//        logger.info("username " + username);
//        logger.info("password " + password);
//        logger.info("virtualhost " + virtualHost);
//        return connectionFactory;
//    }

    @Bean
    public RestTemplate restTemplate(EcmProperties properties) {
        ignoreCertificates();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new BadRequestErrorHandler());
        return restTemplate;
    }

    private void ignoreCertificates() {
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
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
}
