package ru.bpmcons.sbi_elma.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class PublicKeyDto {
    private String realm;
    @JsonProperty("public_key")
    private String publicKey;
}
