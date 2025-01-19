package ru.bpmcons.sbi_elma.models.dto.docLock;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Data
public class BlockObject {
    private String __id;
    private String[] user;
    private String id_ecm;
    private String[] source;
    private Date block_to;
    private Date block_from;
    private String[] file_metadata_collection;
}
