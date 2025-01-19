package ru.bpmcons.sbi_elma.models.dto.docLock;

import lombok.Data;

import java.util.Date;

@Data
public class LockObject {
    private String __id;
    private String[] user;
    private String id_ecm;
    private String[] source;
    private Date block_from;
    private Date block_to;
}
