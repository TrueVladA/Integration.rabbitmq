package ru.bpmcons.sbi_elma.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.CommonSystemRepository;
import ru.bpmcons.sbi_elma.exceptions.PublicKeyNotFoundException;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakRepository {
    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;
    private final CommonSystemRepository commonSystemRepository;

    private final Map<String, PublicKey> keys = new ConcurrentHashMap<>();
    private final Map<String, String> auds = new ConcurrentHashMap<>();

    @PostConstruct
    @Scheduled(fixedDelayString = "86400000", initialDelay = 86400000L)
    public void update() {
        if (StringUtils.hasText(properties.getPksphere())) {
            loadPublicKey("sphere", properties.getPksphere()); // todo remove whe unused
            log.warn("Loading old PK for sphere");
        }
        if (StringUtils.hasText(properties.getPkvirtu())) {
            loadPublicKey("virtu", properties.getPkvirtu());
            log.warn("Loading old PK for virtu  ");
        }
        if (StringUtils.hasText(properties.getPkecm())) {
            loadPublicKey("ECM", properties.getPkecm());
            log.warn("Loading old PK for ECM");
        }
        if (StringUtils.hasText(properties.getPkinsapp())) {
            loadPublicKey("insapp", properties.getPkinsapp());
            log.warn("Loading old PK for insapp");
        }
        if (StringUtils.hasText(properties.getPksso())) {
            loadPublicKey("sso", properties.getPksso());
            log.warn("Loading old PK for sso");
        }

        properties.getApps().forEach((app, cfg) -> {
            if (StringUtils.hasText(cfg.getEcmAuditory())) {
                auds.put(app, cfg.getEcmAuditory());
            }
            if (StringUtils.hasText(cfg.getPublicKey())) {
                loadPublicKey(app, cfg.getPublicKey());
            }
        });

        for (CommonSystem commonSystem : commonSystemRepository.findAll()) {
            if (commonSystem.getKeycloakRealmUrl() != null) {
                loadPublicKey(commonSystem.getAppSysName(), commonSystem.getKeycloakRealmUrl());
            }
        }
    }

    public PublicKey getKey(CommonSystem name) {
        PublicKey publicKey = keys.get(name.getAppSysName());
        if (publicKey == null) {
            throw new PublicKeyNotFoundException(name.getAppSysName());
        }
        return publicKey;
    }

    @NonNull
    public String getAudience(CommonSystem name) {
        return auds.getOrDefault(name.getAppSysName(), properties.getDefaultAuditory());
    }

    private void loadPublicKey(String name, String url) {
        try {
            keys.put(name, fetchPublicKey(url));
        } catch (Exception e) {
            log.error("Failed to load public key for " + name + ", url = " + url, e);
        }
    }

    private PublicKey fetchPublicKey(String url) throws NoSuchAlgorithmException, InvalidKeySpecException {
        ResponseEntity<PublicKeyDto> responseEntity = restTemplate.getForEntity(url, PublicKeyDto.class);
        if (responseEntity.getBody() == null) {
            throw new IllegalStateException("Keycloak returned response with no body, status code " + responseEntity.getStatusCode() + ", url " + url);
        }
        String key = responseEntity.getBody().getPublicKey();
        byte[] decoded = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance(properties.getAlgorithm())
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}
