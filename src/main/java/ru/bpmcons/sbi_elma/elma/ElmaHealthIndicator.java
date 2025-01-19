package ru.bpmcons.sbi_elma.elma;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.properties.EcmProperties;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ElmaHealthIndicator implements HealthIndicator {
    private final RestTemplate restTemplate;
    private final EcmProperties ecmProperties;

    @Override
    public Health health() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ecmProperties.getBearerAuth());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ZonedDateTime start = ZonedDateTime.now();
        ResponseEntity<String> response = restTemplate.exchange(
                ecmProperties.getUrlApp(),
                HttpMethod.GET,
                new HttpEntity<>("", headers),
                String.class
        );
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Health.up()
                    .withDetail("responseTime", Duration.between(start, ZonedDateTime.now()))
                    .build();
        } else {
            return Health.outOfService()
                    .withDetail("responseTime", Duration.between(start, ZonedDateTime.now()))
                    .build();
        }
    }
}
