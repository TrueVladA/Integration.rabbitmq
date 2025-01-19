package ru.bpmcons.sbi_elma.s3;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.exceptions.S3UnavailableException;
import ru.bpmcons.sbi_elma.properties.S3ModuleProperties;
import ru.bpmcons.sbi_elma.s3.dto.*;
import ru.bpmcons.sbi_elma.s3.exception.PresignUrlInvalidException;

import java.util.Map;

@Service
public class S3ModuleService {
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    public S3ModuleService(RestTemplateBuilder restTemplate,
                           S3ModuleProperties s3ModuleProperties) {

        RetryTemplate retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> ex = Map.of(
                ResourceAccessException.class, true,
                HttpServerErrorException.ServiceUnavailable.class, true,
                HttpServerErrorException.BadGateway.class, true,
                HttpServerErrorException.GatewayTimeout.class, true
        );
        var policy = new ExponentialRandomBackOffPolicy();
        policy.setInitialInterval(s3ModuleProperties.getRetryInitial().toMillis());
        policy.setMaxInterval(s3ModuleProperties.getRetryMax().toMillis());
        policy.setMultiplier(s3ModuleProperties.getRetryMultiplier());
        retryTemplate.setBackOffPolicy(policy);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(s3ModuleProperties.getRetryMaxAttempts(), ex));
        this.retryTemplate = retryTemplate;

        this.restTemplate = restTemplate
                .rootUri(s3ModuleProperties.getUrl() + s3ModuleProperties.getPort())
                .build();
    }

    public String refreshPresign(String url, Method method) {
        try {
            return retryTemplate.execute(context -> restTemplate.postForEntity(
                    "/api/refresh-presign",
                    new RefreshPresignRequest(url, method),
                    RefreshPresignResponse.class
            ).getBody().getUrl());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new S3UnavailableException("Нет подключения к сервису s3");
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new PresignUrlInvalidException();
            }
            throw new S3UnavailableException(e.getResponseBodyAsString());
        }
    }

    @Nullable
    public PresignResponse presign(String file, Method method, Bucket bucket, String md5, S3Metadata metadata) {
        try {
            return retryTemplate.execute(context -> restTemplate.postForEntity(
                    "/api/files/" + file + "/generate-presign",
                    new PresignRequest(bucket, method, metadata.toAttributes(), md5),
                    PresignResponse.class
            ).getBody());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new S3UnavailableException("Нет подключения к сервису s3");
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new S3UnavailableException(e.getResponseBodyAsString());
        }
    }

    public PreviewFile createPreview(Bucket bucket, String file, String ext) {
        try {
            return restTemplate.postForEntity(
                    "/api/files/" + file + "/generate-preview",
                    new GeneratePreviewRequest(bucket, ext),
                    PreviewFile.class
            ).getBody();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new S3UnavailableException("Нет подключения к сервису s3");
            }
            throw new S3UnavailableException(e.getResponseBodyAsString());
        }
    }

    public void archive(String file) {
        if (file.isBlank()) {
            return;
        }

        try {
            restTemplate.postForEntity(
                    "/api/files/" + file + "/archive",
                    null,
                    String.class
            );
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new S3UnavailableException("Нет подключения к сервису s3");
            }
            throw new S3UnavailableException(e.getResponseBodyAsString());
        }
    }

    public void unarchive(String file) {
        if (file.isBlank()) {
            return;
        }

        try {
            restTemplate.postForEntity(
                    "/api/files/" + file + "/unarchive",
                    null,
                    String.class
            );
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new S3UnavailableException("Нет подключения к сервису s3");
            }
            throw new S3UnavailableException(e.getResponseBodyAsString());
        }
    }
}
