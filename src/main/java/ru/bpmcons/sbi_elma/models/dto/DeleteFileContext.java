package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;

@Data
public class DeleteFileContext extends ContextFileMetadata {
    private String __id;
    private String[] editor;
    private boolean archive;
}
