package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.productLine}", displayName = "Линейки страховых продуктов")
@EqualsAndHashCode(callSuper = true)
public class ProductLine extends DictionaryBase {
    @JsonProperty("code_product_line")
    private String code;
    @JsonProperty("product_line")
    private String productLine;
}
