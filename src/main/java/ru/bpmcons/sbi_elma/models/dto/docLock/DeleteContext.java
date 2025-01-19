package ru.bpmcons.sbi_elma.models.dto.docLock;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Data
@Builder
public class DeleteContext {
    private String id_ecm;
    private String[] source;
    private String[] user;
    private String[] creator_editor;
    private Date block_from;
    private Date block_to;
    private String user_data;
    private String[] file_metadata_collection;
}
