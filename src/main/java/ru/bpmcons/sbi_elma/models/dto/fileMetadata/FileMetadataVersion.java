package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import lombok.Data;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
public class FileMetadataVersion {
    private int ver;
    private String id_as;
    private String file_name;
    private String categories;
    private Date create_date;
    private Date change_date;
    private String version_number;
    private String url_as;
    @HideFromLogs
    private String url_file;
    @HideFromLogs
    private String url_preview;
    private String file_size;
    private String crc;
    @SerializeAsArray
    private String file;
    @SerializeAsArray
    private String preview;
    private boolean archive;
    @SerializeAsArray
    private String source;
    @SerializeAsArray
    private String status;
    @SerializeAsArray
    private String esign;
    @SerializeAsArray
    private String project;
}
