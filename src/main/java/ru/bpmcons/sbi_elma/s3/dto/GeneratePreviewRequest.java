package ru.bpmcons.sbi_elma.s3.dto;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@RequiredArgsConstructor
public class GeneratePreviewRequest {
    @Nullable
    private final Bucket bucket;
    private final String fileExt;
}

