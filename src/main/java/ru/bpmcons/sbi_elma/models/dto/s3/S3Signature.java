package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;

@Data
public class S3Signature {
    private String algorithm;
    private String credential;
    private String date;
    private String expires;
    private String signedHeaders;
    private String signature;
    @HideFromLogs
    private String url_file;
    private String method;
}
