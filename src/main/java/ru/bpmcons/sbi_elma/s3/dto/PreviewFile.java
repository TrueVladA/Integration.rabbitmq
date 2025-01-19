package ru.bpmcons.sbi_elma.s3.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class PreviewFile {
    private String ecmId;
    private String fileId;
}
