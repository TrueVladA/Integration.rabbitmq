package ru.bpmcons.sbi_elma.keycloak;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.keycloak.exception.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KeycloakJwtParser {
    private final KeycloakRepository keycloakRepository;

    public KeycloakJwtInfo parse(String token, CommonSystem system, boolean ignoreExpires) {
        try {
            return parseInternal(token, system, ignoreExpires);
        } catch (MalformedJwtException e) {
            throw new JwtInvalidException();
        } catch (SignatureException e) {
            throw new JwtSignatureInvalidException();
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException();
        } catch (JwtException e) {
            throw new JwtCommonException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private KeycloakJwtInfo parseInternal(String token, CommonSystem system, boolean ignoreExpires) {
        Claims claims = Jwts.parser()
                .setSigningKey(keycloakRepository.getKey(system))
                .setAllowedClockSkewSeconds(ignoreExpires ? Integer.MAX_VALUE : 0)
                .parseClaimsJws(token)
                .getBody(); // also checks exp, if present
        validateJwtParameters(claims);

        Object aud = claims.get("aud");
        if (aud == null) {
            throw new JwtAudienceInvalidException();
        }
        if (aud.getClass() == String.class) {
            if (!Objects.equals(aud, keycloakRepository.getAudience(system))) {
                throw new JwtAudienceInvalidException();
            }
        } else if (List.class.isAssignableFrom(aud.getClass())) {
            if (!((List<String>) aud).contains(keycloakRepository.getAudience(system))) {
                throw new JwtAudienceInvalidException();
            }
        } else if (String[].class.isAssignableFrom(aud.getClass())) {
            List<String> aud1 = Arrays.asList((String[]) aud);
            if (!aud1.contains(keycloakRepository.getAudience(system))) {
                throw new JwtAudienceInvalidException();
            }
        } else {
            throw new JwtAudienceInvalidException();
        }

        return new KeycloakJwtInfo(
                ((List<String>) claims.get("realm_access", Map.class).get("roles")).toArray(String[]::new),
                claims.get("email", String.class),
                claims.get("name", String.class)
        );
    }

    private static void validateJwtParameters(Claims claims) {
        List<String> missingParameters = new ArrayList<>();
        if (claims.getExpiration() == null || claims.getExpiration().getTime() < 0) { // exp should be present
            missingParameters.add("exp");
        }
        if (!StringUtils.hasText(claims.get("email", String.class))) {
            missingParameters.add("email");
        }
        if (!StringUtils.hasText(claims.get("name", String.class))) {
            missingParameters.add("name");
        }
        if (!StringUtils.hasText(claims.getAudience())) {
            missingParameters.add("aud");
        }
        if (claims.get("realm_access", Map.class) == null) {
            missingParameters.add("realm_access");
        }
        if (!missingParameters.isEmpty()) {
            throw new JwtParameterNotFoundException(missingParameters);
        }
    }
}
