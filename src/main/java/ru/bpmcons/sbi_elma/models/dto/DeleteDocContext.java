package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;

@Data
public class DeleteDocContext {
    private boolean archive;
    @HideFromLogs
    private String url_file;
    @HideFromLogs
    private String url_preview;
    private String[] editor;
}
