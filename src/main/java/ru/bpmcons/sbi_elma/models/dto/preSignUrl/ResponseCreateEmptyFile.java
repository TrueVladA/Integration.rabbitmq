package ru.bpmcons.sbi_elma.models.dto.preSignUrl;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseCreateEmptyFile extends ResponseHeaderFromEcm {
    private File file;
}
