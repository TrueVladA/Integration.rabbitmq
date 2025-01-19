package ru.bpmcons.sbi_elma.ecm.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FileUploadStatus {
    @JsonProperty("uploaded")
    UPLOADED,
    @JsonProperty("uploading")
    UPLOADING,
    @JsonProperty("not_uploaded")
    NOT_UPLOADED,
    @JsonProperty("error")
    ERROR,
}
