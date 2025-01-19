package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;

import java.util.HashMap;

@Data
public class MoveBtwBucketsDto {
    private String id_file_metadata;
    private String url_source;
    private String url_target;
    private String preview;
//    private HashMap<String, String> indexUrls;
}
