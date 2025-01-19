package ru.bpmcons.sbi_elma.ecm.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
public class FileMetadataRow {
    @SerializeAsArray
    @JsonProperty("file_metadata")
    private String fileMetadata;
    @SerializeAsArray
    @JsonProperty("file_type")
    private String oldFileType;


    @SerializeAsArray
    @JsonProperty("ftype")
    private String fileType;
    @JsonProperty("fname")
    private String fileName;
    @JsonProperty("categories")
    private String categories;
}
