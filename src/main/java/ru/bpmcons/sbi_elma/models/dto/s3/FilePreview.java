package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;

@Data
public class FilePreview {
    private int index;
    @HideFromLogs
    private String url_file;
    private String file_format;
    private String url_preview;
    private String id;
}
