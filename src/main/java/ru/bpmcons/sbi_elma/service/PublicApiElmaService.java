package ru.bpmcons.sbi_elma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.exceptions.BadRequestException;
import ru.bpmcons.sbi_elma.exceptions.EcmUnauthorizedException;
import ru.bpmcons.sbi_elma.exceptions.EcmUnavailableException;
import ru.bpmcons.sbi_elma.properties.EcmProperties;

import java.util.function.Supplier;

@Component
public class PublicApiElmaService {
    Logger logger = LoggerFactory.getLogger(PublicApiElmaService.class);

    private final EcmProperties ecmProperties;
    private final RestTemplate restTemplate;

    public PublicApiElmaService(EcmProperties ecmProperties,
                                RestTemplate restTemplate) {
        this.ecmProperties = ecmProperties;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> doPost(String body,
                                         String path,
                                         String typeDoc,
                                         String method) {
        return doPost(body, ecmProperties.getUrlApp() + path + "/" + typeDoc + method);
    }


    public ResponseEntity<String> doPost(String body, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ecmProperties.getBearerAuth());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity(body, headers);
        return execute(() -> restTemplate.postForEntity(
                url,
                request,
                String.class
        ));
    }

    public ResponseEntity<String> doGet(String url) {
        return execute(() -> restTemplate.getForEntity(
                url,
                String.class
        ));
    }

    private ResponseEntity<String> execute(Supplier<ResponseEntity<String>> request) {
        ResponseEntity<String> response = null;
        try {
            for (int i = 0; i < ecmProperties.getErrorRetries(); i++) {
                response = request.get();
                if (response.getStatusCode().is5xxServerError() || response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    logger.error("Получен ошибочный ответ от ecm " + response.getStatusCode() + ": " + response.getBody() + "! Попытка " + (i + 1) + " из " + ecmProperties.getErrorRetries());
                    Thread.sleep(ecmProperties.getErrorRetryTimeout().toMillis());
                } else {
                    break;
                }
            }
            if (response.getStatusCode().is5xxServerError() || response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new EcmUnavailableException("Ошибка api ecm");
            }
            if (response.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                throw new EcmUnauthorizedException();
            }
        } catch (BadRequestException e) {
            logger.debug(e.getBody());
            logger.debug(e.getHttpStatus().name());
            logger.debug(e.getHeaders().toString());
            response = new ResponseEntity<>(e.getBody(), e.getHeaders(), e.getHttpStatus());
        } catch (ResourceAccessException e) {
            throw new EcmUnavailableException("ECM временно не доступно, повторите попытку позже");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.debug(response.getBody());
        return response;
    }

    public ResponseEntity<String> uploadEmptyFile(String fileName, String uuid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ecmProperties.getBearerAuth());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        byte[] bytes = new byte[]{0x00};
        int length = bytes.length;
        headers.set("Content-Range", "bytes 0-" + length + "/" + length);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(fileName)
//                .filename(uuid)
                .build();

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<String> request = new HttpEntity(body, headers);
        String url = ecmProperties.getPathToDisk() + "/upload?hash=" + uuid;
        logger.debug(url);
        return restTemplate.postForEntity(url,
                request,
                String.class);
    }
}
