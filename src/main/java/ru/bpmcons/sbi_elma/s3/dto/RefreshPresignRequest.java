package ru.bpmcons.sbi_elma.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RefreshPresignRequest {
    private String url;
    private Method method;
}

