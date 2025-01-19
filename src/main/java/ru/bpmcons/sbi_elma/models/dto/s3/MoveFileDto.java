package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;

import java.util.List;

@Data
public class MoveFileDto {
    private List<MoveBtwBucketsDto> request;
    private String oldBucket;
    private String newBucket;
    private String method;
}
