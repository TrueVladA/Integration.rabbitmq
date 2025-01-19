package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@DictionaryEntity(name = "${sys_names.commonSystem}", displayName = "Системы-источники")
public class CommonSystem extends DictionaryBase {
    @JsonProperty("app_sysname")
    private String appSysName;
    @JsonProperty("app_name")
    private String appName;
    @Nullable
    @JsonProperty("keycloak_realm_url")
    private String keycloakRealmUrl;
}
