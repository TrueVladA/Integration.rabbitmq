package ru.bpmcons.sbi_elma.models.dto.doc;

import lombok.Data;

@Data
public class Result {
    private ResponseFromPublicApi[] result;
}
