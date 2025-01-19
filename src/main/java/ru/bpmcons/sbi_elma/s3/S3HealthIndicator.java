package ru.bpmcons.sbi_elma.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.properties.S3ModuleProperties;

@Service
@RequiredArgsConstructor
public class S3HealthIndicator implements HealthIndicator {
    private final RestTemplate restTemplate;
    private final S3ModuleProperties s3ModuleProperties;

    @Override
    public Health health() {
        ResponseEntity<String> entity = restTemplate.getForEntity(
                s3ModuleProperties.getUrl() + s3ModuleProperties.getPort(),
                String.class
        );
        if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Health.up().build();
        } else {
            return Health.outOfService().build();
        }
    }
}
