package ru.bpmcons.sbi_elma.models.dto.productLine;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;
import ru.bpmcons.sbi_elma.models.dto.generralized.ProductLine;

@Data
public class ResponseFromEcmCreateProductLine extends ResponseHeaderFromEcm {
    private ProductLine item;
}
