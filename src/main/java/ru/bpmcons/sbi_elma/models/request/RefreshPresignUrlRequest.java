package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
@Valid
@EqualsAndHashCode(callSuper = true)
public class RefreshPresignUrlRequest extends AuthenticatedRequestBase {
    @JsonProperty("expired_links")
    private List<Item> links;

    @Data
    @Valid
    public static final class Item {
        @NotBlank
        @JsonProperty("url")
        private String url;

        @Nullable
        @JsonProperty("properties")
        private Map<String, String> properties;
    }
}
