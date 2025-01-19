package ru.bpmcons.sbi_elma.s3.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class PresignRequest {
    @Nullable
    private final Bucket bucket;
    @NonNull
    private final Method method;
    @Nullable
    private final Map<String, String> attributes;
    @Nullable
    private final String contentMd5;
}
