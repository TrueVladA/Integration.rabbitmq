package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@DictionaryEntity(name = "${sys_names.products}", displayName = "Страховой продукт")
@EqualsAndHashCode(callSuper = true)
public class Product extends DictionaryBase {
    @JsonProperty("code_product")
    private String code;
    @JsonProperty("product_name")
    private String productName;
    @SerializeAsArray
    @JsonProperty("line")
    private String line;
}
