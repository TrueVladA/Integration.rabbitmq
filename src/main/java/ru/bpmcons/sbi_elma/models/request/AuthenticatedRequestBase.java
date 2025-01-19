package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.message.authentication.JwtTokenContainer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthenticatedRequestBase extends RequestBase implements JwtTokenContainer {
    @Valid
    @JsonProperty("jwt_token")
    private JwtToken jwtToken;

    @Data
    public static final class JwtToken {
        @NotBlank
        @JsonProperty("access_token")
        private String accessToken;
    }
}
