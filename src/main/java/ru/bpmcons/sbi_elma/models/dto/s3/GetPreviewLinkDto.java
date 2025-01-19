package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;

@Data
public class GetPreviewLinkDto {
    private FilePreview[] array_files;
}
