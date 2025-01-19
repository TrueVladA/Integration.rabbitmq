package ru.bpmcons.sbi_elma.models.dto.docLock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Date;

@Data
@Builder
public class CreateContext {
    private String id_ecm;
    private String[] source;
    private String[] user;
    private String[] creator_editor;
    private Date block_from;
    private Date block_to;
    private String user_data;
    private String[] file_metadata_collection;
}
